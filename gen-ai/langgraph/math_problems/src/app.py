from typing import Annotated
from typing_extensions import TypedDict
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langchain_openai import ChatOpenAI
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from typing import List, Any, Optional, Dict
from pydantic import BaseModel, Field
import uuid
import asyncio
from datetime import datetime
from opentelemetry import trace
import openlit

tracer = trace.get_tracer("langgraph-example")
openlit.init(environment="test")

class State(TypedDict):
    messages: Annotated[List[Any], add_messages]
    question: str
    solution: str
    grade: str
    rationale: str

class MathQuestion(BaseModel):
    mathematics_branch: str = Field(description="Which branch of mathematics the question is part of.")
    rationale: str = Field(description="Why you chose this question and what you hope students will learn from it.")
    question: str = Field(description="The math question")

class AssignmentSolution(BaseModel):
    solution: str = Field(description="The solution to the math problem provided by the student.")

class AssignmentResult(BaseModel):
    grade: str = Field(description="The grade you've given to the student assignment.")
    rationale: str = Field(description="An explanation of why you gave the grade that you did.")

class MathProblems:
    def __init__(self):
        self.teacher_llm_with_output = None
        self.student_llm_with_output = None
        self.teaching_assistant_llm_with_output = None
        self.graph = None

    async def setup(self):

        teacher_llm = ChatOpenAI(model="gpt-4o-mini")
        self.teacher_llm_with_output = teacher_llm.with_structured_output(MathQuestion)

        student_llm = ChatOpenAI(model="gpt-4o-mini")
        self.student_llm_with_output = student_llm.with_structured_output(AssignmentSolution)

        teaching_assistant_llm = ChatOpenAI(model="gpt-4o-mini")
        self.teaching_assistant_llm_with_output = teaching_assistant_llm.with_structured_output(AssignmentResult)

        await self.build_graph()

    def teacher(self, state: State) -> Dict[str, Any]:

        INSTRUCTIONS = f"""
            You're a seasoned teacher with a knack for keeping students
            engaged with interesting and compelling math problems.
            "Your task is to create a question suitable for Grade 8 math students.
            "You should first choose which branch of mathematics the question will be part of.
            "Then provide a rationale for the question and what you hope students will learn from it.
            "The question itself should be in markdown format."""

        # Add in the system message
        found_system_message = False
        messages = state["messages"]
        for message in messages:
            if isinstance(message, SystemMessage):
                message.content = INSTRUCTIONS
                found_system_message = True

        if not found_system_message:
            messages = [SystemMessage(content=INSTRUCTIONS)] + messages

        # Invoke the LLM with tools
        result = self.teacher_llm_with_output.invoke(messages)

        # Return updated state
        new_state = {
            "messages": [{"role": "assistant", "content": f"The question is: {result.question}"}],
            "question": result.question
        }

        return new_state

    def student(self, state: State) -> Dict[str, Any]:

        INSTRUCTIONS = f"""You're a meticulous Grade 8 student with a keen eye for detail. You're known for
            your ability to apply logic to just about any math problem the teacher
            gives you.  Yet sometimes, you make things too darn complicated.
            Your task is to review the assigned math question and prepare a solution.
            The answer should be in markdown format and should show the steps you used to find the solution.
            Pass the final solution to the 'Teaching Assistant' agent."""

        # Add in the system message
        found_system_message = False
        messages = state["messages"]
        for message in messages:
            if isinstance(message, SystemMessage):
                message.content = INSTRUCTIONS
                found_system_message = True

        if not found_system_message:
            messages = [SystemMessage(content=INSTRUCTIONS)] + messages

        # Invoke the LLM with tools
        result = self.student_llm_with_output.invoke(messages)

        # Return updated state
        new_state = {
            "messages": [{"role": "assistant", "content": f"The solution is: {result.solution}"}],
            "solution": result.solution,
        }

        return new_state

    def teaching_assistant(self, state: State) -> Dict[str, Any]:

        INSTRUCTIONS = f"""
            You've been a teaching assistant for more than 10 years. When it comes
            to grading student assignments, you're tough, but fair.  You usually provide
            witty comments when grading an assignment.
            Your task is to review the assigned math question and the solution provided by the student.
            Grade the assignment and provide a rationale for the grade.
            The rationale should be in markdown format."""

        # Add in the system message
        found_system_message = False
        messages = state["messages"]
        for message in messages:
            if isinstance(message, SystemMessage):
                message.content = INSTRUCTIONS
                found_system_message = True

        if not found_system_message:
            messages = [SystemMessage(content=INSTRUCTIONS)] + messages

        # Invoke the LLM with tools
        result = self.teaching_assistant_llm_with_output.invoke(messages)

        # Return updated state
        new_state = {
            "messages": [{"role": "assistant", "content": f"The grade is: {result.grade}"}],
            "grade": result.grade,
            "rationale": result.rationale,
        }

        return new_state

    async def build_graph(self):
        # Set up Graph Builder with State
        graph_builder = StateGraph(State)

        # Add nodes
        graph_builder.add_node("teacher", self.teacher)
        graph_builder.add_node("student", self.student)
        graph_builder.add_node("teaching_assistant", self.teaching_assistant)

        # Add edges
        graph_builder.add_edge(START, "teacher")
        graph_builder.add_edge("teacher", "student")
        graph_builder.add_edge("student", "teaching_assistant")
        graph_builder.add_edge("teaching_assistant", END)

        # Compile the graph
        self.graph = graph_builder.compile()

    async def run(self, message):
        state = {
            "messages": message
        }

        with tracer.start_as_current_span("langgraph-example") as current_span:
            result = await self.graph.ainvoke(state)
            print(result["question"])
            print(result["solution"])
            print(result["grade"])
            print(result["rationale"])

def main():
   math_problems = MathProblems()
   asyncio.run(math_problems.setup())
   message = "Create a math question for a grade 8 student"
   asyncio.run(math_problems.run(message))

if __name__ == "__main__":
    main()
