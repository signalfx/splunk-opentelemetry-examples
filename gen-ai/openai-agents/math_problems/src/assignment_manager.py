from agents import Runner
from student_agent import student_agent
from teacher_agent import teacher_agent, MathQuestion
from teaching_assistant_agent import teaching_assistant_agent, AssignmentResult
import asyncio
from opentelemetry import trace
import openlit

tracer = trace.get_tracer("openai-agents")
openlit.init(environment="test")

class AssignmentManager:

    async def run(self):
       with tracer.start_as_current_span("math-assignment") as current_span:
            """ Run the assignment process """
            print("Teacher is generating a math question...")
            math_question = await self.create_question()
            print("Student is preparing a solution to the question")
            solution = await self.prepare_solution(math_question)
            print("Teaching Assistant is grading the solution")
            assignment_result = await self.grade_assignment(solution)
            print(assignment_result)
            return assignment_result


    async def create_question(self) -> MathQuestion:
        """ Create a math question to assign to the student """
        print("Creating a math question...")
        input = "Create a math question for a grade 8 student"
        result = await Runner.run(
            teacher_agent,
            input
        )
        return result.final_output_as(MathQuestion)

    async def prepare_solution(self, question: MathQuestion) -> str:
        """ Prepare a solution to the question """
        input = f"The math question: {question.question}"
        result = await Runner.run(
            student_agent,
            input,
        )
        return str(result.final_output)

    async def grade_assignment(self, solution: str) -> AssignmentResult:
        """ Assigning a grade to the solution """
        input = f"Solution: {solution}"
        result = await Runner.run(
            teaching_assistant_agent,
            solution,
        )

        return result.final_output_as(AssignmentResult)


def main():
   assignment_manager = AssignmentManager()
   asyncio.run(assignment_manager.run())

if __name__ == "__main__":
    main()

