package net.coagulate.GPHUD.Modules.PermissionsGroups;

import net.coagulate.Core.Exceptions.UserException;
import net.coagulate.GPHUD.Data.Audit;
import net.coagulate.GPHUD.Data.PermissionsGroup;
import net.coagulate.GPHUD.Interfaces.Responses.ErrorResponse;
import net.coagulate.GPHUD.Interfaces.Responses.OKResponse;
import net.coagulate.GPHUD.Interfaces.Responses.Response;
import net.coagulate.GPHUD.Modules.Argument.ArgumentType;
import net.coagulate.GPHUD.Modules.Argument.Arguments;
import net.coagulate.GPHUD.Modules.Command.Commands;
import net.coagulate.GPHUD.Modules.Command.Context;
import net.coagulate.GPHUD.Modules.Modules;
import net.coagulate.GPHUD.Modules.URL.URLs;
import net.coagulate.GPHUD.SafeMap;
import net.coagulate.GPHUD.State;
import net.coagulate.SL.Data.User;

import javax.annotation.Nonnull;

/**
 * Add/remove/edit users in a permissions group.
 *
 * @author Iain Price <gphud@predestined.net>
 */
public abstract class Member {

	@URLs(url = "/permissionsgroups/eject", requiresPermission = "Instance.ManagePermissions")
	public static void ejectForm(@Nonnull final State st, @Nonnull final SafeMap values) {
		Modules.simpleHtml(st, "permissionsgroups.eject", values);
	}

	@Nonnull
	@Commands(context = Context.AVATAR, requiresPermission = "Instance.ManagePermissions", description = "Remove a member from a permissions group")
	public static Response eject(@Nonnull final State st,
	                             @Nonnull @Arguments(type = ArgumentType.PERMISSIONSGROUP, description = "Permissions group to remove member from") final
	                             PermissionsGroup permissionsgroup,
	                             @Nonnull @Arguments(description = "Avatar to remove from the group", type = ArgumentType.AVATAR) final
	                                 User avatar
	) {
		if (!permissionsgroup.canEject(st)) { return new ErrorResponse("No permission to eject from this group"); }
		try { permissionsgroup.removeMember(avatar); } catch (@Nonnull final UserException e) {
			return new ErrorResponse("Failed to remove user from permissions group - " + e.getMessage());
		}
		Audit.audit(st, Audit.OPERATOR.AVATAR, avatar, null, "Remove Member", permissionsgroup.getNameSafe(), null, avatar.getName(), "Avatar removed avatar from permissions group");
		return new OKResponse("Removed " + avatar.getName() + " from permissions group " + permissionsgroup.getNameSafe());
	}

	@URLs(url = "/permissionsgroups/invite", requiresPermission = "Instance.ManagePermissions")
	public static void createForm(@Nonnull final State st, @Nonnull final SafeMap values) {
		Modules.simpleHtml(st, "permissionsgroups.invite", values);
	}

	@Nonnull
	@Commands(context = Context.AVATAR, requiresPermission = "Instance.ManagePermissions", description = "Adds a user to a permissions group")
	public static Response invite(@Nonnull final State st,
	                              @Nonnull @Arguments(description = "Permissions group to join avatar to", type = ArgumentType.PERMISSIONSGROUP) final
	                              PermissionsGroup permissionsgroup,
	                              @Nonnull @Arguments(description = "Avatar to join to group", type = ArgumentType.AVATAR) final
	                                  User avatar
	) {
		if (!permissionsgroup.canInvite(st)) { return new ErrorResponse("No permission to invite to this group"); }
		try { permissionsgroup.addMember(avatar); } catch (@Nonnull final UserException e) {
			return new ErrorResponse("Failed to add user to permissions group - " + e.getMessage());
		}
		Audit.audit(st, Audit.OPERATOR.AVATAR, avatar, null, "Add Member", permissionsgroup.getNameSafe(), null, avatar.getName(), "Avatar added avatar to permissions group");
		return new OKResponse("Added " + avatar.getName() + " to permissions group " + permissionsgroup.getNameSafe());
	}

	@URLs(url = "/permissionsgroups/setpermissions", requiresPermission = "Instance.ManagePermissions")
	public static void setPermissionsForm(@Nonnull final State st, @Nonnull final SafeMap values) {
		Modules.simpleHtml(st, "permissionsgroups.setpermissions", values);
	}

	@Nonnull
	@Commands(context = Context.AVATAR, requiresPermission = "Instance.ManagePermissions", description = "Set a users permissions over a permissions group")
	public static Response setPermissions(@Nonnull final State st,
	                                      @Nonnull @Arguments(type = ArgumentType.PERMISSIONSGROUP, description = "Permissions group to set a users permissions in") final
	                                      PermissionsGroup permissionsgroup,
	                                      @Nonnull @Arguments(type = ArgumentType.AVATAR, description = "Avatar in the group to set permissions of") final
	                                          User avatar,
	                                      @Arguments(type = ArgumentType.BOOLEAN, description = "Can the user invite people to this group") final
	                                          Boolean caninvite,
	                                      @Arguments(type = ArgumentType.BOOLEAN, description = "Can the user remove people from this group") final
	                                          Boolean cankick) {
		try { permissionsgroup.setUserPermissions(avatar, caninvite, cankick); } catch (@Nonnull final UserException e) {
			return new ErrorResponse("Failed to set user permissions on group  - " + e.getMessage());
		}
		Audit.audit(st, Audit.OPERATOR.AVATAR, avatar, null, "SetUserPermissions", permissionsgroup.getName(), null, "CanInvite:" + caninvite + " CanKick:" + cankick, "Avatar updated avatars permissions to invite/kick from permissions group");
		return new OKResponse("Set user " + avatar.getName() + " in group " + permissionsgroup.getName() + " to CanInvite:" + caninvite + " CanKick:" + cankick);
	}
}
    
