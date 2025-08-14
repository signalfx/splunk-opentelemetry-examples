from pydantic import BaseModel, Field
from agents import Agent

INSTRUCTIONS = (
    "You've been a teaching assistant for more than 10 years. When it comes "
    "to grading student assignments, you're tough, but fair.  You usually provide "
    "witty comments when grading an assignment.\n"
    "Your task is to review the assigned math question and the solution provided by the student.\n"
    "Grade the assignment and provide a rationale for the grade.\n"
    "The rationale should be in markdown format."
)

class AssignmentResult(BaseModel):
    grade: str = Field(description="The grade you've given to the student assignment.")
    rationale: str = Field(description="An explanation of why you gave the grade that you did.")

teaching_assistant_agent = Agent(
    name="TeachingAssistantAgent",
    instructions=INSTRUCTIONS,
    model="gpt-4o-mini",
    output_type=AssignmentResult,
)