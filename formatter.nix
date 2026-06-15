{ pkgs, pname }:

pkgs.treefmt.withConfig {
  name = pname;

  runtimeInputs = with pkgs; [
    nixfmt
    deadnix
    prettier
    shellcheck
    beautysh
    keep-sorted
    google-java-format
  ];

  settings = {
    # Log level for files treefmt won't format
    on-unmatched = "info";

    formatter = {
      nixfmt = {
        command = "nixfmt";
        options = [ "--strict" ];

        includes = [ "*.nix" ];
      };

      deadnix = {
        priority = 1;
        command = "deadnix";
        options = [ "--edit" ];

        includes = [ "*.nix" ];
      };

      prettier = {
        command = "prettier";
        options = [ "--write" ];

        includes = [
          # keep-sorted start
          "*.cjs"
          "*.css"
          "*.html"
          "*.js"
          "*.json"
          "*.json5"
          "*.jsx"
          "*.md"
          "*.mdx"
          "*.mjs"
          "*.scss"
          "*.ts"
          "*.tsx"
          "*.vue"
          "*.yaml"
          "*.yml"
          # keep-sorted end
        ];
      };

      beautysh = {
        command = "beautysh";
        options = [
          "--indent-size"
          "2"
        ];

        includes = [
          "*.sh"
          ".envrc"
          "bin/hooks/*"
        ];
      };

      shellcheck = {
        priority = 1;
        command = "shellcheck";

        includes = [
          "*.sh"
          "*.bash"
          "*.envrc"
          "*.envrc.*"
          "bin/hooks/*"
        ];
      };

      keep-sorted = {
        priority = 10;
        command = "keep-sorted";

        includes = [ "*" ];
      };

      google-java-format = {
        command = "google-java-format";
        options = [ "--replace" ];

        includes = [ "*.java" ];
      };
    };
  };
}
