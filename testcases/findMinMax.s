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
_findMin:
sw $ra, 0($sp)
sub $sp, $sp, 4
sw $fp, 0($sp)
sub $sp, $sp, 4
sub $sp, $sp, 20
sw $s0, 4($sp)
swc1 $f8, 16($sp)
swc1 $f1, 12($sp)
sw $t0, 8($sp)
swc1 $f0, 20($sp)
move $fp, $sp
sub $sp, $sp, 8
li $s0, 0
sw $s0, _i
before_for_0:
lw $t0, _i
bge $t0, 5, after_for_1
lwc1 $f1, _min
lw $t0, _i
sll $t8, $t0, 2
la $t9, _a
add $t8, $t8, $t9
lwc1 $f0, 0($t8)
swc1 $f0, 0($fp)
c.lt.s $f0, $f1
bc1f if_false_2
lw $t0, _i
sll $t8, $t0, 2
la $t9, _a
add $t8, $t8, $t9
lwc1 $f0, 0($t8)
swc1 $f0, -4($fp)
mov.s $f8, $f0
swc1 $f8, _min
if_false_2:
lw $t0, _i
addi $s0, $t0, 1
sw $s0, _i
j before_for_0
after_for_1:
lwc1 $f0, _min
mfc1 $v0, $f0
j _findMin_epilogue_4
_findMin_epilogue_4:
move $sp, $fp
lw $s0, 4($sp)
lwc1 $f8, 16($sp)
lwc1 $f1, 12($sp)
lw $t0, 8($sp)
lwc1 $f0, 20($sp)
add $sp, $sp, 20
add $sp, $sp, 4
lw $fp, 0($sp)
add $sp, $sp, 4
lw $ra, 0($sp)
jr $ra
_findMax:
sw $ra, 0($sp)
sub $sp, $sp, 4
sw $fp, 0($sp)
sub $sp, $sp, 4
sub $sp, $sp, 20
sw $s0, 4($sp)
swc1 $f8, 16($sp)
swc1 $f1, 12($sp)
sw $t0, 8($sp)
swc1 $f0, 20($sp)
move $fp, $sp
sub $sp, $sp, 8
li $s0, 0
sw $s0, _i
before_for_5:
lw $t0, _i
bge $t0, 5, after_for_6
lw $t0, _i
lwc1 $f1, _max
sll $t8, $t0, 2
la $t9, _a
add $t8, $t8, $t9
lwc1 $f0, 0($t8)
swc1 $f0, 0($fp)
c.le.s $f0, $f1
bc1t if_false_7
lw $t0, _i
sll $t8, $t0, 2
la $t9, _a
add $t8, $t8, $t9
lwc1 $f0, 0($t8)
swc1 $f0, -4($fp)
mov.s $f8, $f0
swc1 $f8, _max
if_false_7:
lw $t0, _i
addi $s0, $t0, 1
sw $s0, _i
j before_for_5
after_for_6:
lwc1 $f0, _max
mfc1 $v0, $f0
j _findMax_epilogue_9
_findMax_epilogue_9:
move $sp, $fp
lw $s0, 4($sp)
lwc1 $f8, 16($sp)
lwc1 $f1, 12($sp)
lw $t0, 8($sp)
lwc1 $f0, 20($sp)
add $sp, $sp, 20
add $sp, $sp, 4
lw $fp, 0($sp)
add $sp, $sp, 4
lw $ra, 0($sp)
jr $ra
.globl main
main:
sw $ra, 0($sp)
sub $sp, $sp, 4
li.s $f8, 0.0
swc1 $f8, __f4
li $s0, 0
sw $s0, __t0
_a_assign_10:
lwc1 $f0, __f4
lw $t0, __t0
sll $t8, $t0, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
addi $t0, $t0, 1
sw $t0, __t0
bne $t0, 5, _a_assign_10
li $s0, 0
sw $s0, _i
li.s $f0, 100.0
swc1 $f0, __f5
mov.s $f8, $f0
swc1 $f8, _min
li.s $f0, 0.0
swc1 $f0, __f6
mov.s $f8, $f0
swc1 $f8, _max
li.s $f0, 100.0
swc1 $f0, __f7
mov.s $f8, $f0
swc1 $f8, _resultMin
li.s $f0, 0.0
swc1 $f0, __f8
mov.s $f1, $f0
swc1 $f1, _resultMax
li.s $f0, 5.0
swc1 $f0, __f9
li $t8, 0
sll $t8, $t8, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
li.s $f0, 6.0
swc1 $f0, __f10
li $t8, 1
sll $t8, $t8, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
li.s $f0, 7.0
swc1 $f0, __f11
li $t8, 2
sll $t8, $t8, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
li.s $f0, 0.0
swc1 $f0, __f12
li $t8, 3
sll $t8, $t8, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
li.s $f0, 10.0
swc1 $f0, __f13
li $t8, 4
sll $t8, $t8, 2
la $t9, _a
add $t8, $t8, $t9
swc1 $f0, 0($t8)
sub $sp, $sp, 4
li $a0, 0
jal _findMin
mtc1 $v0, $f0
add $sp, $sp, 4
swc1 $f0, __f14
mov.s $f8, $f0
swc1 $f8, _resultMin
sub $sp, $sp, 4
li $a0, 0
jal _findMax
mtc1 $v0, $f0
add $sp, $sp, 4
swc1 $f0, __f15
mov.s $f0, $f0
swc1 $f0, _resultMin
sub $sp, $sp, 4
mfc1 $a0, $f0
jal _printf
add $sp, $sp, 4
sub $sp, $sp, 4
mfc1 $a0, $f1
jal _printf
add $sp, $sp, 4
add $sp, $sp, 4
lw $ra, 0($sp)
jal $ra

.data
__f12: .word 0
__f11: .word 0
__f4: .word 0
_a: .space 20
__f10: .word 0
__f5: .word 0
_resultMax: .word 0
_max: .word 0
_i: .word 0
__f15: .word 0
__f14: .word 0
__f13: .word 0
__t0: .word 0
_resultMin: .word 0
__f6: .word 0
__f7: .word 0
__f8: .word 0
_min: .word 0
__f9: .word 0
