import { Slider } from "@material-ui/core";
import React from "react";
import { useServer } from "./APIProvider";

const PowerSlider = () => {
  const { server } = useServer();

  return server ? (
    <Slider
      onChange={(_, value) => {
        server.sendPower((value as number) / 100);
      }}
    />
  ) : (
    <Slider disabled />
  );
};

export default PowerSlider;
