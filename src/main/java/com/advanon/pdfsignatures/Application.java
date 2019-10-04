package com.advanon.pdfsignatures;

import java.util.Arrays;

class Application {
  public static void main(String[] args) {
    ApplicationArguments arguments = new ApplicationArguments(
        Arrays.asList(Arrays.copyOfRange(args, 1, args.length))
    );

    ApplicationCommand command = new ApplicationCommand(
        args[0] == null ? "help" : args[0],
        arguments.parse()
    );

    command.execute();
  }
}
