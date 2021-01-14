import React from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import Box from "@material-ui/core/Box";
import Link from "@material-ui/core/Link";
import { Button, Input, Slider } from "@material-ui/core";

export default function App() {
  return (
    <Container maxWidth="sm">
      <Box my={4}>
        <Input placeholder="P4ssw0rd" />
        <Button>Connect</Button>
        <Slider />
      </Box>
    </Container>
  );
}
