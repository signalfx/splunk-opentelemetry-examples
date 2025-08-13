#!/usr/bin/env python
import sys
import warnings
import openlit
from crewai.utilities import Logger

from datetime import datetime

from math_problems.crew import MathProblems

my_logger = Logger(verbose=True)

warnings.filterwarnings("ignore", category=SyntaxWarning, module="pysbd")
openlit.init(environment="test")

# This main file is intended to be a way for you to run your
# crew locally, so refrain from adding unnecessary logic into this file.
# Replace with inputs you want to test with, it will automatically
# interpolate any tasks and agents information

def run():
    """
    Run the crew.
    """
    inputs = {
        'grade': '8'
    }
    
    try:
        my_logger.log(
            "info",
            "About to kickoff the crew...",
            color="blue"
        )
        result = MathProblems().crew().kickoff(inputs=inputs)
        my_logger.log(
            "info",
            result.raw,
            color="blue"
        )
    except Exception as e:
        raise Exception(f"An error occurred while running the crew: {e}")


def train():
    """
    Train the crew for a given number of iterations.
    """
    inputs = {
        'grade': '8'
    }
    try:
        MathProblems().crew().train(n_iterations=int(sys.argv[1]), filename=sys.argv[2], inputs=inputs)

    except Exception as e:
        raise Exception(f"An error occurred while training the crew: {e}")

def replay():
    """
    Replay the crew execution from a specific task.
    """
    try:
        MathProblems().crew().replay(task_id=sys.argv[1])

    except Exception as e:
        raise Exception(f"An error occurred while replaying the crew: {e}")

def test():
    """
    Test the crew execution and returns the results.
    """
    inputs = {
        'grade': '8'
    }
    
    try:
        MathProblems().crew().test(n_iterations=int(sys.argv[1]), eval_llm=sys.argv[2], inputs=inputs)

    except Exception as e:
        raise Exception(f"An error occurred while testing the crew: {e}")
