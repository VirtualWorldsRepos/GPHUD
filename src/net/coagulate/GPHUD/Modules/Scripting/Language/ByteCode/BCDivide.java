package net.coagulate.GPHUD.Modules.Scripting.Language.ByteCode;

import net.coagulate.GPHUD.Modules.Scripting.Language.GSVM;
import net.coagulate.GPHUD.Modules.Scripting.Language.ParseNode;
import net.coagulate.GPHUD.State;

import java.util.List;

public class BCDivide extends ByteCode {
	public BCDivide(ParseNode n) {
		super(n);
	}

	// Pop two, op, push result
	public String explain() { return "Divide (Pop two, divide, push result)"; }
	public void toByteCode(List<Byte> bytes) {
		bytes.add(InstructionSet.Divide.get());
	}

	@Override
	public void execute(State st, GSVM vm, boolean simulation) {
		ByteCodeDataType arg1 = vm.pop();
		ByteCodeDataType arg2 = vm.pop();
		vm.push(arg1.divide(arg2));
	}
}
