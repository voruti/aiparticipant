{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs?ref=nixpkgs-unstable";

    blueprint = {
      url = "github:numtide/blueprint";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    nix-vscode-extensions = {
      url = "github:nix-community/nix-vscode-extensions";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  # Load the blueprint
  outputs =
    inputs:
    inputs.blueprint {
      inherit inputs;
      nixpkgs.overlays = [ inputs.nix-vscode-extensions.overlays.default ];
    };
}
