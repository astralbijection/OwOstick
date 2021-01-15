import { Button, Input } from "@material-ui/core";
import Box from "@material-ui/core/Box";
import React, { useState } from "react";
import { useServer } from "./APIProvider";

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
