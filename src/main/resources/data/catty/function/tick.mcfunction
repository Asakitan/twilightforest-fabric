# Catty - master tick
# 1) Mob buff scan in twilight realm
execute in catty:twilight_realm as @e[type=#catty:twilight_buff_targets,tag=!catty_buffed] at @s run function catty:buff_apply

# 2) Portal particle/sound emission - per tick for smooth visuals
execute as @e[type=marker,tag=catty_portal] at @s run function catty:portal/emit_particles

# 3) Sound loop (every 80 ticks = 4 sec)
scoreboard players add #portal_sound catty.const 1
execute if score #portal_sound catty.const matches 80.. as @e[type=marker,tag=catty_portal] at @s run playsound minecraft:block.portal.ambient ambient @a[distance=..20] ~ ~ ~ 0.4 0.6
execute if score #portal_sound catty.const matches 80.. run scoreboard players set #portal_sound catty.const 0

# 4) Portal scan (throttled to every 10 ticks)
scoreboard players add #portal_tick catty.const 1
execute if score #portal_tick catty.const matches 10.. run function catty:portal/scan
