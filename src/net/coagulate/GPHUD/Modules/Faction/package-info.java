@ModuleDefinition(description = "Provides support for factions/groups/tribes/clan/whatever",implementation = "net.coagulate.GPHUD.Modules.Faction.FactionsModule")
@Permissions(name = "SetFaction",description = "Ability to force set a player's faction")
@Permissions(name="Configure",description="Configure the faction module")        
@Permissions(name="Create",description="Configure the faction module")        
@Permissions(name="Delete",description="Configure the faction module")        
@SideMenus(name = "Factions",priority = 50,url = "/factions")
@Permissions(name="SetOwner",description="May set the owner on a faction")
@KVS(name="XPPerCycle",defaultvalue = "5",description="Maximum faction XP the player may earn in a cycle.  Requires Experience module.  Set to zero to disable.",editpermission = "Faction.Configure",scope = KVSCOPE.COMPLETE,type = KVTYPE.INTEGER,hierarchy = KVHIERARCHY.DELEGATING,template=false)
@Pools(name = "FactionXP",description = "Faction awarded XP")
@KVS(name="XPCycleLength",defaultvalue="6.75",description="Number of days per faction XP cycle",editpermission = "Faction.Configure",scope = KVSCOPE.COMPLETE,type = KVTYPE.FLOAT,hierarchy = KVHIERARCHY.DELEGATING,template=false)
package net.coagulate.GPHUD.Modules.Faction;

import net.coagulate.GPHUD.Modules.KV.KVHIERARCHY;
import net.coagulate.GPHUD.Modules.KV.KVS;
import net.coagulate.GPHUD.Modules.KV.KVSCOPE;
import net.coagulate.GPHUD.Modules.KV.KVTYPE;
import net.coagulate.GPHUD.Modules.Module.ModuleDefinition;
import net.coagulate.GPHUD.Modules.Permission.Permissions;
import net.coagulate.GPHUD.Modules.Pool.Pools;
import net.coagulate.GPHUD.Modules.SideMenu.SideMenus;

