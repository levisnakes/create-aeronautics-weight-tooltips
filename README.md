# Create Aeronautics: Weight Tooltips

Shows the block weight on every item tooltip — including the ones Create: Aeronautics leaves blank.

NeoForge 1.21.1 · client-side only · works on any server.

## The problem

Create: Aeronautics (via its bundled `simulated` mod) prints a physics line on block tooltips like
`Light (0.5 kpg)` or `Super Heavy (4 kpg)`. Two things are missing:

1. **Blocks at the default weight show nothing.** Its tooltip code returns `null` for a mass of
   exactly `1.0`, so most of the game's blocks have no weight line at all. You can't tell "this
   weighs the normal amount" apart from "this block has no weight data".
2. **It's gated behind Engineer's Goggles.** `displayProperties` defaults to `GOGGLES`, so with the
   goggles off you see no weights at all.

## What this adds

A weight line for any block item that doesn't already have one, using the exact tier names, colours,
thresholds and number formatting Aeronautics uses, plus a new **Normal** tier for the 1 kpg default:

| Weight | Tier | Colour |
| --- | --- | --- |
| 0 kpg | Weightless | gray |
| ≤ 0.25 | Super Light | aqua |
| ≤ 0.5 | Light | green |
| = 1 | **Normal** *(new)* | white |
| < 4 | Heavy | yellow |
| < 50 | Super Heavy | red |
| ≥ 50 | Absurdly Heavy | red |

Lines Aeronautics already wrote are never duplicated — the check matches on the translated tier
names, so a `Floating (n kpg)` line (which shares the `kpg` unit) is not mistaken for a weight.

Blocks you can walk through (torches, plants, pressure plates) are reported as `Weightless`, matching
how Aeronautics treats them.

## Config

`config/aeroweights-client.toml`

| Option | Default | Meaning |
| --- | --- | --- |
| `display` | `ALWAYS` | `ALWAYS`, `SHIFT` (only while holding shift), or `NEVER` |
| `showValue` | `true` | `Normal (1 kpg)` vs just `Normal` |
| `showWeightless` | `true` | Whether 0 kpg blocks get a line |

## How it works

Weights come from Sable's `physics_block_properties` datapack registry, read through the same
`BlockStateExtension#sable$getProperty(MASS)` path Aeronautics itself uses. The line is appended from
`ItemTooltipEvent`, which is where Aeronautics appends its own, so it lands in the same place.

Sable is PolyForm Shield licensed, so nothing of it is bundled or checked in — the lookup is done by
reflection and the mod quietly disables itself if the classes aren't there.

## Building

```bash
./gradlew build
```

Output: `build/libs/aeroweights-1.0.0.jar`. No third-party jars are needed to compile.
