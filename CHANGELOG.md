# Changelog

## 1.0.0

- Adds a weight line to block tooltips that Create: Aeronautics leaves blank, including every block
  at the 1 kpg default (a new **Normal** tier).
- Reuses Aeronautics' own tier names, colours, thresholds and number formatting, and its
  translations where they exist.
- Never duplicates a weight line Aeronautics already wrote.
- Blocks without collision report as `Weightless`, matching Aeronautics.
- Config: `display` (ALWAYS / SHIFT / NEVER), `showValue`, `showWeightless`.
