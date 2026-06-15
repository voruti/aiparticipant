{
  lib ? pkgs.lib,
  pkgs,
}:

pkgs.mkShell {
  packages = [
    pkgs.nixd # a language server
    pkgs.jdk25
  ];

  env = {
    JAVA_HOME = pkgs.jdk25;
  };

  shellHook = ''
    [ -f .env ] && export $(grep -v '^#' .env | xargs)

    ${lib.getExe pkgs.git} config --local core.hooksPath "$(${lib.getExe pkgs.git} rev-parse --show-toplevel)/bin/hooks"

    ${lib.getExe pkgs.git} fetch --all --tags --prune || true
    echo
    ${lib.getExe pkgs.git} status
  '';
}
