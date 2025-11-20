import 'dart:async';

import 'package:dartchess/dartchess.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:lichess_mobile/src/model/common/chess.dart';
import 'package:lichess_mobile/src/model/common/uci.dart';
import 'package:lichess_mobile/src/model/engine/engine.dart';
import 'package:lichess_mobile/src/model/engine/work.dart';
import 'package:logging/logging.dart';
import 'package:multistockfish/multistockfish.dart';

/// Service to interface with Stockfish engine for computer game move generation.
class ComputerGameEngine {
  ComputerGameEngine() : _log = Logger('ComputerGameEngine');

  final Logger _log;
  StockfishEngine? _engine;

  /// Initialize the Stockfish engine for computer games.
  Future<void> initialize() async {
    if (_engine != null) {
      return; // Already initialized
    }

    try {
      _log.info('Initializing Stockfish engine for computer game');
      // Use SF16 flavor for computer games (smaller, no NNUE needed for lower levels)
      _engine = StockfishEngine(StockfishFlavor.sf16);
      
      // Start the engine to ensure it's ready
      await _engine!.start(Work(
        variant: Variant.standard,
        threads: 1,
        searchTime: const Duration(milliseconds: 100),
        multiPv: 1,
        initialPosition: Chess.initial,
        steps: IList(),
        path: UciPath.empty,
      )).first;
      
      _engine!.stop();
      _log.info('Stockfish engine initialized successfully');
    } catch (e, s) {
      _log.severe('Failed to initialize Stockfish engine', e, s);
      rethrow;
    }
  }

  /// Get the best move for the current position at the specified difficulty level.
  /// 
  /// Difficulty levels 1-8 are mapped to Stockfish parameters:
  /// - Level 1-2: Depth 1-3 (Beginner)
  /// - Level 3-4: Depth 5-8 (Intermediate)
  /// - Level 5-6: Depth 10-13 (Advanced)
  /// - Level 7-8: Depth 15-20 (Expert)
  Future<Move?> getMove(Position position, int level, Variant variant) async {
    if (_engine == null) {
      await initialize();
    }

    if (_engine == null) {
      throw StateError('Engine failed to initialize');
    }

    final depth = _getDepthForLevel(level);
    final searchTime = Duration(milliseconds: 50 * depth);

    _log.info('Getting move at level $level (depth: $depth, time: ${searchTime.inMilliseconds}ms)');

    try {
      // Create work for the engine
      final work = Work(
        variant: variant,
        threads: 1,
        searchTime: searchTime,
        multiPv: 1,
        initialPosition: position,
        steps: IList(),
        path: UciPath.empty,
      );

      // Start engine computation
      final evalStream = _engine!.start(work);

      Move? bestMove;
      int pvCount = 0;

      // Collect eval results up to target depth
      await for (final (_, eval) in evalStream) {
        if (eval.depth >= depth || pvCount > 50) {
          bestMove = eval.bestMove;
          break;
        }
        pvCount++;
      }

      _engine!.stop();

      if (bestMove == null) {
        _log.warning('No move found by engine');
      }

      return bestMove;
    } catch (e, s) {
      _log.severe('Error getting move from engine', e, s);
      _engine!.stop();
      return null;
    }
  }

  /// Map difficulty level (1-8) to engine search depth.
  int _getDepthForLevel(int level) {
    return switch (level) {
      1 => 1,   // Beginner - very weak
      2 => 3,   // Beginner
      3 => 5,   // Novice
      4 => 8,   // Novice/Intermediate
      5 => 10,  // Intermediate
      6 => 13,  // Advanced
      7 => 15,  // Expert
      8 => 20,  // Expert - full strength
      _ => 10,  // Default to medium
    };
  }

  /// Dispose the engine.
  Future<void> dispose() async {
    if (_engine != null) {
      _log.info('Disposing computer game engine');
      await _engine!.dispose();
      _engine = null;
    }
  }
}
