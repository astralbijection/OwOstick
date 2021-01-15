import { InputLabel } from "@material-ui/core";
import Box from "@material-ui/core/Box";
import Container from "@material-ui/core/Container";
import React from "react";
import APIProvider from "./APIProvider";
import ConnectionForm from "./ConnectionForm";
import PowerSlider from "./PowerSlider";

export default function App() {
  return (
    <APIProvider endpoint="ws://localhost:8888/api/controller">
      <Container maxWidth="sm">
        <ConnectionForm />
        <Box my={4}>
          <InputLabel>Power</InputLabel>
          <PowerSlider />
        </Box>
      </Container>
    </APIProvider>
  );
}
