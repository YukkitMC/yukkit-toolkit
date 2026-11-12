# Contributing to Yukkit Toolkit

Thanks for helping make Yukkit Toolkit better. We value practical ideas, reproducible reports, and thoughtful changes over the size of a pull request.

## Before you start

- Read the [README](README.md), [Code of Conduct](CODE_OF_CONDUCT.md), and [MIT License](LICENSE).
- Check existing [issues](https://github.com/YukkitMC/yukkit-toolkit/issues) and [pull requests](https://github.com/YukkitMC/yukkit-toolkit/pulls) to avoid duplicating work.
- For major changes, open a feature proposal **before** investing in a large implementation.
- Never publish security vulnerabilities in a public issue. Follow [SECURITY.md](SECURITY.md).

## Reporting bugs

Use the **Bug report** issue template. Include:

1. What happened and what you expected.
2. Steps that reliably reproduce the problem.
3. Minecraft version, mod loader, Yukkit Toolkit version or commit, and operating system.
4. Relevant logs or screenshots with tokens, account information, and personal data removed.

Do not claim an unreleased or unimplemented module is broken; use a feature discussion instead.

## Requesting features

Use the **Feature request** template. Explain the problem, how the feature improves the player experience, and what limitations or server rules might apply. A clear use case is more valuable than a list of unrelated features.

## Pull request workflow

1. Open or select a relevant issue for meaningful changes.
2. Fork the repository on GitHub if you do not have direct write access.
3. Create a focused branch, such as `feat/time-changer` or `fix/config-load`.
4. Implement your changes and update tests or documentation where applicable.
5. Run any build, formatting, and test tasks documented for the current codebase.
6. Open a pull request using the provided template and explain what changed and how you verified it.
7. Respond to review feedback. Maintainers decide when a change is ready to merge.

GitHub forks are the normal route to submit pull requests; they do not prevent a contribution from being merged into the official project.

## Engineering standards

- Favor small, readable modules and explicit configuration behavior.
- Avoid unnecessary dependencies and performance-heavy background work.
- Keep compatibility claims tied to versions that have actually been tested.
- Make optional features disabled or configurable where sensible.
- Never add telemetry, network requests, auto-updaters, or data collection without clear disclosure and review.
- Do not embed secrets, proprietary assets you lack rights to, or code copied without a compatible license.
- Avoid functionality specifically intended to bypass access controls or anti-cheat systems.

A detailed Java build and formatting guide will be published when the project structure is established. Until then, do not assume a particular loader, Java version, or build command is officially supported.

## Ownership and licensing

The repository currently uses the **[MIT License](LICENSE)**. By submitting code for inclusion, you confirm that you have the right to contribute it and agree for the submitted contribution to be distributed under MIT. You retain copyright in your original work; submitting a pull request does not automatically transfer ownership to YukkitMC.

Maintainers can request changes, decline a contribution, or close inactive proposals. Review and acceptance times are not guaranteed.

Thanks for building with us.
