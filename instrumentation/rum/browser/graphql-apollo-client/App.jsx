// Import everything needed to use the `useQuery` hook
import React from "react";
import { gql } from "@apollo/client";
import { useQuery } from "@apollo/client/react";

const GET_BOOKS = gql`
    query GetBooks {
      books {
        title
        author
      }
    }
`;


function DisplayBooks() {
    const { loading, error, data } = useQuery(GET_BOOKS);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error : {error.message}</p>;

    return data.books.map(({ title, author }) => (
        <div key={title}>
            <h3>{title}</h3>
            <br />
            <b>Author:</b>
            <p>{author}</p>
            <br />
        </div>
    ));
}

export default function App() {
    return (
        <div>
            <h2>My first Apollo app 🚀</h2>
            <br />
            <DisplayBooks />
        </div>
    );
}
