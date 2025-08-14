from pydantic import BaseModel, Field
from agents import Agent

INSTRUCTIONS = (
    "You're a seasoned teacher with a knack for keeping students "
    "engaged with interesting and compelling math problems.\n"
    "Your task is to create a question suitable for Grade 8 math students.\n"
    "You should first choose which branch of mathematics the question will be part of.\n"
    "Then provide a rationale for the question and what you hope students will learn from it.\n"
    "The question itself should be in markdown format."
)

class MathQuestion(BaseModel):
    mathematics_branch: str = Field(description="Which branch of mathematics the question is part of.")
    rationale: str = Field(description="Why you chose this question and what you hope students will learn from it.")
    question: str = Field(description="The math question")

teacher_agent = Agent(
    name="TeacherAgent",
    instructions=INSTRUCTIONS,
    model="gpt-4o-mini",
    output_type=MathQuestion,
)