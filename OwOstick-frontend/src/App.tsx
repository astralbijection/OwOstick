import React from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import Box from "@material-ui/core/Box";
import Link from "@material-ui/core/Link";
import { Button, Input, Slider } from "@material-ui/core";
import { OwOServer } from "./api/OwOServer";
import APIProvider from "./APIProvider";
import ConnectionForm from "./ConnectionForm";

export default function App() {
  return (
    <APIProvider endpoint="ws://localhost:8888/api/controller">
      <Container maxWidth="sm">
        <ConnectionForm />
        <Box my={4}>
          <Slider />
        </Box>
      </Container>
    </APIProvider>
  );
}
