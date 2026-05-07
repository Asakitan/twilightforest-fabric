# Catty Twilight Realm - load
scoreboard objectives add catty.const dummy
scoreboard objectives add catty.depth dummy
scoreboard players set #portal_tick catty.const 0
scoreboard players set #portal_sound catty.const 0
data modify storage catty:portal _initialized set value 1b
tellraw @a [{"text":"[Catty 暮色刀剑] ","color":"dark_purple","bold":true},{"text":"数据包已加载 ฅฅ","color":"light_purple"}]
tellraw @a [{"text":"  → 入口仪式: ","color":"gray"},{"text":"挖水池 + 周围 4 花 + 扔钻石入水","color":"aqua"}]
