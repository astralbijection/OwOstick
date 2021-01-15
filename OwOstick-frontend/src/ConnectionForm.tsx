import React, { useState } from "react";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import Box from "@material-ui/core/Box";
import Link from "@material-ui/core/Link";
import { Button, Input, Slider } from "@material-ui/core";
import { OwOServer } from "./api/OwOServer";
import APIProvider, { useServer } from "./APIProvider";

const ConnectionForm = () => {
  const { connect } = useServer();
  const [password, setPassword] = useState("");
  return (
    <Box>
      <Input
        placeholder="P4ssw0rd"
        value={password}
        onChange={(ev) => setPassword(ev.target.value)}
      />
      <Button onClick={() => connect(password)}>Connect</Button>
    </Box>
  );
};

export default ConnectionForm;
