import { InputLabel } from "@material-ui/core";
import Box from "@material-ui/core/Box";
import Container from "@material-ui/core/Container";
import { assert } from "console";
import React from "react";
import APIProvider from "./APIProvider";
import ConnectionForm from "./ConnectionForm";
import PowerSlider from "./PowerSlider";

export default function App() {
  const host =
    process.env.NODE_ENV === "production"
      ? process.env.REACT_APP_HOST
      : "localhost:8888";
  return (
    <APIProvider endpoint={`ws://${host}/api/controller`}>
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
