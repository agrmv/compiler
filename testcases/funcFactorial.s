.text
_printi:
li $v0, 1
syscall
jal $ra
_printf:
swc1 $f12, 0($sp)
mtc1 $a0, $f12
li $v0, 2
syscall
lwc1 $f12, 0($sp)
jal $ra
_fact:
sw $ra, 0($sp)
sub $sp, $sp, 4
sw $fp, 0($sp)
sub $sp, $sp, 4
sub $sp, $sp, 8
sw $t1, 8($sp)
sw $t0, 4($sp)
move $fp, $sp
sub $sp, $sp, 12
move $t0, $a0
bne $t0, 0, if_false_0
li $v0, 1
j _fact_epilogue_4
if_false_0:
move $t0, $a0
bne $t0, 1, if_false_2
li $v0, 1
j _fact_epilogue_4
if_false_2:
move $t1, $a0
sub $t0, $t1, 1
sw $t0, 0($fp)
sw $a0, 20($fp)
sub $sp, $sp, 4
move $a0, $t0
jal _fact
move $t0, $v0
lw $a0, 20($fp)
add $sp, $sp, 4
sw $t0, -4($fp)
move $t0, $t0
sw $t0, _a
mult $t1, $t0
mflo $t0
sw $t0, -8($fp)
move $v0, $t0
j _fact_epilogue_4
_fact_epilogue_4:
move $sp, $fp
lw $t1, 8($sp)
lw $t0, 4($sp)
add $sp, $sp, 8
add $sp, $sp, 4
lw $fp, 0($sp)
add $sp, $sp, 4
lw $ra, 0($sp)
jr $ra
_factf:
sw $ra, 0($sp)
sub $sp, $sp, 4
sw $fp, 0($sp)
sub $sp, $sp, 4
sub $sp, $sp, 12
swc1 $f1, 8($sp)
swc1 $f0, 4($sp)
sw $t0, 12($sp)
move $fp, $sp
sub $sp, $sp, 40
mtc1 $a0, $f0
li $t8, 0
mtc1 $t8, $f1
cvt.s.w $f1, $f1
swc1 $f1, 0($fp)
c.le.s $f0, $f1
bc1f if_false_5
li $t0, 1
sw $t0, -4($fp)
mtc1 $t0, $f0
cvt.s.w $f0, $f0
swc1 $f0, -8($fp)
mfc1 $v0, $f0
j _factf_epilogue_9
if_false_5:
mtc1 $a0, $f1
li $t8, 1
mtc1 $t8, $f0
cvt.s.w $f0, $f0
swc1 $f0, -12($fp)
c.le.s $f1, $f0
bc1f if_false_7
li $t0, 1
sw $t0, -16($fp)
mtc1 $t0, $f0
cvt.s.w $f0, $f0
swc1 $f0, -20($fp)
mfc1 $v0, $f0
j _factf_epilogue_9
if_false_7:
mtc1 $a0, $f1
li $t8, 1
mtc1 $t8, $f0
cvt.s.w $f0, $f0
swc1 $f0, -24($fp)
sub.s $f0, $f1, $f0
swc1 $f0, -28($fp)
sw $a0, 24($fp)
sub $sp, $sp, 4
mfc1 $a0, $f0
jal _factf
mtc1 $v0, $f0
lw $a0, 24($fp)
add $sp, $sp, 4
swc1 $f0, -32($fp)
mov.s $f0, $f0
swc1 $f0, _d
mul.s $f0, $f1, $f0
swc1 $f0, -36($fp)
mfc1 $v0, $f0
j _factf_epilogue_9
_factf_epilogue_9:
move $sp, $fp
lwc1 $f1, 8($sp)
lwc1 $f0, 4($sp)
lw $t0, 12($sp)
add $sp, $sp, 12
add $sp, $sp, 4
lw $fp, 0($sp)
add $sp, $sp, 4
lw $ra, 0($sp)
jr $ra
.globl main
main:
sw $ra, 0($sp)
sub $sp, $sp, 4
sub $sp, $sp, 4
li $a0, 6
jal _fact
move $t0, $v0
add $sp, $sp, 4
sw $t0, __t5
move $t0, $t0
sw $t0, _c
sub $sp, $sp, 4
move $a0, $t0
jal _printi
add $sp, $sp, 4
li.s $f0, 6.0
swc1 $f0, __f8
sub $sp, $sp, 4
mfc1 $a0, $f0
jal _factf
mtc1 $v0, $f0
add $sp, $sp, 4
swc1 $f0, __f9
mov.s $f1, $f0
swc1 $f1, _f
sub $sp, $sp, 4
mfc1 $a0, $f1
jal _printf
add $sp, $sp, 4
mtc1 $t0, $f0
cvt.s.w $f0, $f0
swc1 $f0, __f10
c.eq.s $f0, $f1
bc1f if_false_10
sub $sp, $sp, 4
li $a0, 1
jal _printi
add $sp, $sp, 4
if_false_10:
add $sp, $sp, 4
lw $ra, 0($sp)
jal $ra

.data
_a: .word 0
__f10: .word 0
_c: .word 0
_d: .word 0
_f: .word 0
__t5: .word 0
__f8: .word 0
__f9: .word 0
