# Common Troubleshooting

## When using Simple Voice Chat, disguised players' voices can't be heard unless in a group
See [Permissions](./Permissions.md), setting `feathermorph.disguise_use_real_uuid` for them would help.

## Disguise actions not playing / Disguise property doesn't have a visual effect where they should
The server is probably running on Mod Renderer. Please try to execute `/fm stat` and check whether the value of `Default Backend` is `client`.

If so, installing [PacketEvents](https://modrinth.com/plugin/packetevents) and restarting the server should help resolve the issue.

## Server is using a custom nametag plugin, and the nametag is glitching
Unfortunately, since FeatherMorph doesn't mess with nametags or any scoreboard features, actions might be required on their side.

See [Discussion about player tag (nametag) plugins](https://github.com/NiFeather/FeatherMorph/discussions/38) for compatibility report.
