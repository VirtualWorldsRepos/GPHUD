@ModuleDefinition(description = "Provides health support", defaultDisable = true)
@Permissions(name = "config", description = "Configure the health module",power = Permission.POWER.MEDIUM)
@Permissions(name = "sethealth", description = "May set any users health value",power = Permission.POWER.LOW)
@KVS(name = "allowreset", description = "May user reset their own health", defaultvalue = "false", editpermission = "health.config", scope = KVSCOPE.COMPLETE, type = KVTYPE.BOOLEAN, hierarchy = KVHIERARCHY.DELEGATING, template = false)
@KVS(name = "initialhealth", description = "Initial health value", defaultvalue = "10", editpermission = "health.config", scope = KVSCOPE.COMPLETE, type = KVTYPE.INTEGER, hierarchy = KVHIERARCHY.CUMULATIVE, template = true)
@KVS(name = "health", description = "Current health", editpermission = "health.sethealth", scope = KVSCOPE.COMPLETE, defaultvalue = "10", template = true, type = KVTYPE.INTEGER, hierarchy = KVHIERARCHY.DELEGATING)
@KVS(name = "allowNegative", defaultvalue = "false", editpermission = "health.config", description = "Allow health to go negative (otherwise stops at zero)", hierarchy = KVHIERARCHY.DELEGATING, scope = KVSCOPE.COMPLETE, type = KVTYPE.BOOLEAN, template = false)
@KVS(name="allowSelfSet",description="Allow character to set their own health",editpermission="health.config",hierarchy = KVHIERARCHY.DELEGATING,type = KVTYPE.BOOLEAN,defaultvalue = "false",scope = KVSCOPE.COMPLETE,template = false)
package net.coagulate.GPHUD.Modules.Health;

import net.coagulate.GPHUD.Modules.KV.KVHIERARCHY;
import net.coagulate.GPHUD.Modules.KV.KVS;
import net.coagulate.GPHUD.Modules.KV.KVSCOPE;
import net.coagulate.GPHUD.Modules.KV.KVTYPE;
import net.coagulate.GPHUD.Modules.Module.ModuleDefinition;
import net.coagulate.GPHUD.Modules.Permission;
import net.coagulate.GPHUD.Modules.Permission.Permissions;

