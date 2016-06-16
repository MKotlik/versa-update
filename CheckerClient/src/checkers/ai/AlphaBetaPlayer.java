package checkers.ai;

import checkers.*;

import java.util.*;

/*
 * This is a skeleton for an alpha beta checkers player.
 * Employees the AlphaBeta search algorithm, should be better than just min-max
 */

public class AlphaBetaPlayer extends CheckersPlayer implements playerGrade
{

	protected int count;
	protected Evaluator sbe;

	public AlphaBetaPlayer(String name, int side)
	{ 
		super(name, side);
		// Use BetterEvaluator to score terminal nodes
		sbe = new BetterEvaluator();
	}

	public void calculateMove(int[] bs)
	{

		Random generator = new Random();

		Evaluate evaluate = new Evaluate(bs, side);
		/* Get all the possible moves for this player on the provided board state */
		List<Move> possibleMoves = evaluate.getAllPossibleMoves();

		/* If this player has no moves, return out */
		if (possibleMoves.size() == 0)
			return;

		Move bestMove = null;
		int bestScore = Integer.MIN_VALUE;
		
		for(int curDepth = 1; curDepth < this.depthLimit; curDepth+=2){
			/* Find best board state among those reachable from one move */
			bestScore = Integer.MIN_VALUE;
			bestMove = null;
			count = 0;
			
			for (Move move : possibleMoves)
			{
				/* Execute the move so we can score the board state resulting from 
				 * the move */
				
				evaluate.execute(move);

				int score = minValue(Integer.MIN_VALUE, Integer.MAX_VALUE, curDepth - 1, evaluate);

				/* Update bestMove if score > bestScore */
				if (score > bestScore)
				{
					bestMove = move;
					bestScore = score;

				}
				else if(score == bestScore && generator.nextDouble() > .7){
					bestMove = move;
					bestScore = score;
				}

				/* Revert the move so we can score additional board states. */
				evaluate.revert();
			}

			setMove(bestMove);

		}
	}



	private int maxValue(int alpha, int beta, int depth, Evaluate bs){

		int maxSide = side;

		List<Move> possibleMoves = bs.getAllPossibleMoves();

		if (possibleMoves.size() == 0 || depth == 0){
			int score = sbe.eval(bs.D);
			if(maxSide == CheckersConstants.BLUE)
				return -score;
			else
				return score;
		}

		for (Move move : possibleMoves)
		{
			/* Execute the move so we can score the board state resulting from 
			 * the move */

			bs.execute(move);
			alpha = Math.max(alpha, (minValue(alpha, beta, (depth-1), bs)));
			bs.revert();

			if(alpha >= beta){
				count++;
				return beta;
			}
		}

		/* Revert the move so we can score additional board states. */

		return alpha;
	}


	private int minValue(int alpha, int beta, int depth, Evaluate bs){

		List<Move> possibleMoves = bs.getAllPossibleMoves();

		if (possibleMoves.size() == 0 || depth == 0){
			int score = sbe.eval(bs.D);
			if(side == CheckersConstants.BLUE)
				return -score;
			else
				return score;
		}

		for (Move move : possibleMoves)
		{
			/* Execute the move so we can score the board state resulting from 
			 * the move */

			bs.execute(move);
			beta = Math.min(beta, (maxValue(alpha, beta, (depth-1), bs)));
			bs.revert();
			/* Revert the move so we can score additional board states. */

			if(alpha >= beta){
				count++;
				return alpha;
			}
		}
		return beta;
	}

	public int getCount()
	{
		return count;
	}
}
