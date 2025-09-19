## FeatherMorph custom data usages

You can add these to the item's custom data to change how FeatherMorph process the output item.

### `feathermorph:skip_magic_bottle_setup`
If set to `true`, plugin will skip this item when processing outputs.

If not set, default to `false`

### `feathermorph:always_append_reference_tooltip`
If set to `true`, a reference tooltip will always append after the item lore, telling the player what disguise this bottle will give.

If set to `false`, reference tooltip will only add if there's no lore or the lore is empty.

If not set, default to `false`