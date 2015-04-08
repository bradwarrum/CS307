package core;

import java.util.EnumSet;

//Bitfield pattern taken from StackOverflow question
//http://codereview.stackexchange.com/questions/7594/boolean-flags-encoded-as-integer-implemented-with-enumset
public class Permissions {
	public enum Flag {
		CAN_INVITE(1<<0),
		CAN_MODIFY_INVENTORY(1<<1),
		CAN_READ_INVENTORY(1<<2),
		CAN_MODIFY_LISTS(1<<3),
		CAN_READ_LISTS(1<<4),
		CAN_MODIFY_RECIPES(1<<5),
		CAN_READ_RECIPES(1<<6);
	
		public final int bit;
		private Flag(int value) {
			bit = value;
		}
	}
	
	private final EnumSet<Flag> permissions = EnumSet.noneOf(Flag.class);
	private final int intflags;
	
	public static Permissions all() {
		return new Permissions(0x7F);
	}
	
	public Permissions(Flag...flagSet) {
		int value = 0;
		for (Flag f : flagSet) {
			permissions.add(f);
			value += f.bit;
		}
		intflags = value;
	}
	
	public Permissions(int flags) {
		intflags = flags;
		for (Flag f : Flag.values()) {
			if ((intflags & f.bit) > 0) {
				permissions.add(f);
			}
		}
	}
	
	public int asInt() {
		return intflags;
	}
	
	/**
	 * Check that a user has specific permissions.
	 * @param flagSet The permission flags to check for.
	 * @return Returns true if the Permissions object has all the flags in the flagSet.
	 */
	public boolean has(Flag...flagSet) {
		for (Flag f : flagSet) {
			if (!permissions.contains(f)) return false;
		}
		return true;
	}
	
}
