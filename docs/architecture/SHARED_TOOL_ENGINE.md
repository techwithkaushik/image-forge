# Shared Tool Engine Architecture

ImageForge treats dashboard entries as configurations over reusable processing families.

## Flow

Dashboard Tool -> ToolDefinition -> ProcessingFamily -> Family Engine -> ImageOperation -> Repository -> Storage

A tool title is presentation data. Routing is based on ToolDefinition.destination and ProcessingFamily, not UI string heuristics.

## Kotlin design rules

- Prefer composition over deep inheritance.
- Use data classes for immutable configuration.
- Use sealed interfaces for closed operation/result hierarchies.
- Use enums for finite family/capability sets.
- Use value classes for lightweight type-safe identifiers.
- Use extension functions for small stateless utilities.
- Use overloads only when signatures remain unambiguous.
- Keep long-running image work cancellable and off the main thread.
- Keep family engines independent from Compose.

## Reuse rule

Multiple dashboard tools may share one family engine with different presets/options.

Examples:

- Pixel resize, percentage resize, A4, passport and social presets reuse resize infrastructure.
- Target-size compression tools reuse compression infrastructure.
- Format-specific conversion tools reuse conversion infrastructure.

A new dashboard entry should normally require a catalog definition and preset, not a new algorithm.

## Migration

The existing Resize Image implementation is the first family implementation. Remaining families are connected incrementally after the shared catalog/routing foundation is stable.

## Non-goals

This foundation does not pretend that every tool has identical processing requirements. Tool-specific capabilities and presets remain explicit so specialized workflows can add behavior without duplicating the common engine.