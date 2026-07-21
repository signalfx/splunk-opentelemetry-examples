import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MockedProvider } from "@apollo/client/testing/react";
import App, { GET_BOOKS, GET_BOOK_TITLES } from "../App";

const mocks = [
    {
        request: { query: GET_BOOKS },
        result: {
            data: {
                books: [
                    { title: "The Awakening", author: "Kate Chopin" },
                    { title: "City of Glass", author: "Paul Auster" },
                ],
            },
        },
    },
    {
        request: { query: GET_BOOK_TITLES },
        result: {
            data: {
                books: [{ title: "The Awakening" }, { title: "City of Glass" }],
            },
        },
    },
];

describe("App", () => {
    it("renders books and titles from GraphQL queries", async () => {
        render(
            <MockedProvider mocks={mocks} addTypename={false}>
                <App />
            </MockedProvider>
        );

        expect(screen.getByText("My first Apollo app 🚀")).toBeInTheDocument();
        expect(await screen.findByText("Kate Chopin")).toBeInTheDocument();
        expect(await screen.findByText("Paul Auster")).toBeInTheDocument();
        expect(screen.getAllByText("The Awakening")).toHaveLength(2);
        expect(screen.getAllByText("City of Glass")).toHaveLength(2);
    });
});
