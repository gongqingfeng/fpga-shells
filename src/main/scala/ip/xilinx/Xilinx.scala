// See LICENSE for license details.
package sifive.fpgashells.ip.xilinx

import Chisel._
import chisel3.core.{Input, Output, attach}
import chisel3.experimental.{Analog}
import freechips.rocketchip.util.{ElaborationArtefacts}

import sifive.blocks.devices.pinctrl.{BasePin}
import sifive.fpgashells.clocks._

//========================================================================
// This file contains common devices used by our Xilinx FPGA flows and some
// BlackBox modules used in the Xilinx FPGA flows
//========================================================================

//-------------------------------------------------------------------------
// mmcm
//-------------------------------------------------------------------------
/** mmcm: This is generated by the Xilinx IP Generation Scripts */

class mmcm extends BlackBox {
  val io = new Bundle {
    val clk_in1  = Input(Clock())
    val clk_out1 = Output(Clock())
    val clk_out2 = Output(Clock())
    val clk_out3 = Output(Clock())
    val resetn   = Input(Bool())
    val locked   = Output(Bool())
  }
}

//-------------------------------------------------------------------------
// reset_sys
//-------------------------------------------------------------------------
/** reset_sys: This is generated by the Xilinx IP Generation Scripts */

class reset_sys extends BlackBox {
  val io = new Bundle {
    val slowest_sync_clk     = Input(Clock())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  }
}

//-------------------------------------------------------------------------
// reset_mig
//-------------------------------------------------------------------------
/** reset_mig: This is generated by the Xilinx IP Generation Scripts */

class reset_mig extends BlackBox {
  val io = new Bundle {
    val slowest_sync_clk     = Input(Clock())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  }
}

//-------------------------------------------------------------------------
// PowerOnResetFPGAOnly
//-------------------------------------------------------------------------
/** PowerOnResetFPGAOnly -- this generates a power_on_reset signal using
  * initial blocks.  It is synthesizable on FPGA flows only.
  */

// This is a FPGA-Only construct, which uses
// 'initial' constructions
class PowerOnResetFPGAOnly extends BlackBox {
  val io = new Bundle {
    val clock = Input(Clock())
    val power_on_reset = Output(Bool())
  }
}

object PowerOnResetFPGAOnly {
  def apply (clk: Clock): Bool = {
    val por = Module(new PowerOnResetFPGAOnly())
    por.io.clock := clk
    por.io.power_on_reset
  }
}


//-------------------------------------------------------------------------
// vc707_sys_clock_mmcm
//-------------------------------------------------------------------------
//IP : xilinx mmcm with "NO_BUFFER" input clock
class Series7MMCM(c : PLLParameters) extends BlackBox with PLLInstance {
  val io = new Bundle {
    val clk_in1   = Clock(INPUT)
    val clk_out1  = if (c.req.size >= 1) Some(Clock(OUTPUT)) else None
    val clk_out2  = if (c.req.size >= 2) Some(Clock(OUTPUT)) else None
    val clk_out3  = if (c.req.size >= 3) Some(Clock(OUTPUT)) else None
    val clk_out4  = if (c.req.size >= 4) Some(Clock(OUTPUT)) else None
    val clk_out5  = if (c.req.size >= 5) Some(Clock(OUTPUT)) else None
    val clk_out6  = if (c.req.size >= 6) Some(Clock(OUTPUT)) else None
    val clk_out7  = if (c.req.size >= 7) Some(Clock(OUTPUT)) else None
    val reset     = Bool(INPUT)
    val locked    = Bool(OUTPUT)
  }

  val moduleName = c.name
  override def desiredName = c.name

  def getClocks = Seq() ++ io.clk_out1 ++ io.clk_out2 ++ 
                           io.clk_out3 ++ io.clk_out4 ++ 
                           io.clk_out5 ++ io.clk_out6 ++ 
                           io.clk_out7
  def getInput = io.clk_in1
  def getReset = Some(io.reset)
  def getLocked = io.locked
  def getClockNames = Seq.tabulate (c.req.size) { i =>
    s"${c.name}/inst/mmcm_adv_inst/CLKOUT${i}" 
  }

  val used = Seq.tabulate(7) { i =>
    s" CONFIG.CLKOUT${i+1}_USED {${i < c.req.size}} \\\n"
  }.mkString

  val outputs = c.req.zipWithIndex.map { case (r, i) =>
    s""" CONFIG.CLKOUT${i+1}_REQUESTED_OUT_FREQ {${r.freqMHz}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_PHASE {${r.phaseDeg}} \\
       | CONFIG.CLKOUT${i+1}_REQUESTED_DUTY_CYCLE {${r.dutyCycle}} \\
       |""".stripMargin
  }.mkString

  val checks = c.req.zipWithIndex.map { case (r, i) =>
    val f = if (i == 0) "_F" else ""
    val phaseMin = r.phaseDeg - r.phaseErrorDeg
    val phaseMax = r.phaseDeg + r.phaseErrorDeg
    val freqMin = r.freqMHz * (1 - r.freqErrorPPM / 1000000)
    val freqMax = r.freqMHz * (1 + r.freqErrorPPM / 1000000)
    s"""set jitter [get_property CONFIG.CLKOUT${i+1}_JITTER [get_ips ${moduleName}]]
       |if {$$jitter > ${r.jitterPS}} {
       |  puts "Output jitter $$jitter ps exceeds required limit of ${r.jitterPS}"
       |  exit 1
       |}
       |set phase [get_property CONFIG.MMCM_CLKOUT${i}_PHASE [get_ips ${moduleName}]]
       |if {$$phase < ${phaseMin} || $$phase > ${phaseMax}} {
       |  puts "Achieved phase $$phase degrees is outside tolerated range ${phaseMin}-${phaseMax}"
       |  exit 1
       |}
       |set div2 [get_property CONFIG.MMCM_CLKOUT${i}_DIVIDE${f} [get_ips ${moduleName}]]
       |set freq [expr { ${c.input.freqMHz} * $$mult / $$div1 / $$div2 }]
       |if {$$freq < ${freqMin} || $$freq > ${freqMax}} {
       |  puts "Achieved frequency $$freq MHz is outside tolerated range ${freqMin}-${freqMax}"
       |  exit 1
       |}
       |puts "Achieve frequency $$freq MHz phase $$phase degrees jitter $$jitter ps"
       |""".stripMargin
  }.mkString

  ElaborationArtefacts.add(s"${moduleName}.vivado.tcl",
    s"""create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name \\
       | ${moduleName} -dir $$ipdir -force
       |set_property -dict [list \\
       | CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \\
       | CONFIG.PRIM_SOURCE {No_buffer} \\
       | CONFIG.USE_PHASE_ALIGNMENT {${c.input.feedback}} \\
       | CONFIG.NUM_OUT_CLKS {${c.req.size.toString}} \\
       | CONFIG.PRIM_IN_FREQ {${c.input.freqMHz.toString}} \\
       | CONFIG.CLKIN1_JITTER_PS {${c.input.jitter}} \\
       |${used}${outputs}] [get_ips ${moduleName}]
       |set mult [get_property CONFIG.MMCM_CLKFBOUT_MULT_F [get_ips ${moduleName}]]
       |set div1 [get_property CONFIG.MMCM_DIVCLK_DIVIDE [get_ips ${moduleName}]]
       |${checks}""".stripMargin)
}

//-------------------------------------------------------------------------
// vc707reset
//-------------------------------------------------------------------------

class vc707reset() extends BlackBox
{
  val io = new Bundle{
    val areset = Bool(INPUT)
    val clock1 = Clock(INPUT)
    val reset1 = Bool(OUTPUT)
    val clock2 = Clock(INPUT)
    val reset2 = Bool(OUTPUT)
    val clock3 = Clock(INPUT)
    val reset3 = Bool(OUTPUT)
    val clock4 = Clock(INPUT)
    val reset4 = Bool(OUTPUT)
  }
}

//-------------------------------------------------------------------------
// vcu118_sys_clock_mmcm
//-------------------------------------------------------------------------
//IP : xilinx mmcm with "NO_BUFFER" input clock

class vcu118_sys_clock_mmcm0 extends BlackBox {
  val io = new Bundle {
    val clk_in1   = Bool(INPUT)
    val clk_out1  = Clock(OUTPUT)
    val clk_out2  = Clock(OUTPUT)
    val clk_out3  = Clock(OUTPUT)
    val clk_out4  = Clock(OUTPUT)
    val clk_out5  = Clock(OUTPUT)
    val clk_out6  = Clock(OUTPUT)
    val clk_out7  = Clock(OUTPUT)
    val reset     = Bool(INPUT)
    val locked    = Bool(OUTPUT)
  }

  ElaborationArtefacts.add(
    "vcu118_sys_clock_mmcm0.vivado.tcl",
    """create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name vcu118_sys_clock_mmcm0 -dir $ipdir -force
    set_property -dict [list \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_SOURCE {No_buffer} \
    CONFIG.CLKOUT2_USED {true} \
    CONFIG.CLKOUT3_USED {true} \
    CONFIG.CLKOUT4_USED {true} \
    CONFIG.CLKOUT5_USED {true} \
    CONFIG.CLKOUT6_USED {true} \
    CONFIG.CLKOUT7_USED {true} \
    CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {12.5} \
    CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {25} \
    CONFIG.CLKOUT3_REQUESTED_OUT_FREQ {37.5} \
    CONFIG.CLKOUT4_REQUESTED_OUT_FREQ {50} \
    CONFIG.CLKOUT5_REQUESTED_OUT_FREQ {100} \
    CONFIG.CLKOUT6_REQUESTED_OUT_FREQ {150.000} \
    CONFIG.CLKOUT7_REQUESTED_OUT_FREQ {75} \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_IN_FREQ {250.000} \
    CONFIG.CLKIN1_JITTER_PS {50.0} \
    CONFIG.MMCM_DIVCLK_DIVIDE {5} \
    CONFIG.MMCM_CLKFBOUT_MULT_F {24.000} \
    CONFIG.MMCM_CLKIN1_PERIOD {4.000} \
    CONFIG.MMCM_CLKOUT0_DIVIDE_F {96.000} \
    CONFIG.MMCM_CLKOUT1_DIVIDE {48} \
    CONFIG.MMCM_CLKOUT2_DIVIDE {32} \
    CONFIG.MMCM_CLKOUT3_DIVIDE {24} \
    CONFIG.MMCM_CLKOUT4_DIVIDE {12} \
    CONFIG.MMCM_CLKOUT5_DIVIDE {8} \
    CONFIG.MMCM_CLKOUT6_DIVIDE {16} \
    CONFIG.NUM_OUT_CLKS {7} \
    CONFIG.CLKOUT1_JITTER {213.008} \
    CONFIG.CLKOUT1_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT2_JITTER {179.547} \
    CONFIG.CLKOUT2_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT3_JITTER {164.187} \
    CONFIG.CLKOUT3_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT4_JITTER {154.688} \
    CONFIG.CLKOUT4_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT5_JITTER {135.165} \
    CONFIG.CLKOUT5_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT6_JITTER {126.046} \
    CONFIG.CLKOUT6_PHASE_ERROR {154.678} \
    CONFIG.CLKOUT7_JITTER {142.781} \
    CONFIG.CLKOUT7_PHASE_ERROR {154.678}] [get_ips vcu118_sys_clock_mmcm0] """
  )
}

class vcu118_sys_clock_mmcm1 extends BlackBox {
  val io = new Bundle {
    val clk_in1   = Bool(INPUT)
    val clk_out1  = Clock(OUTPUT)
    val clk_out2  = Clock(OUTPUT)
    val reset     = Bool(INPUT)
    val locked    = Bool(OUTPUT)
  }

  ElaborationArtefacts.add(
    "vcu118_sys_clock_mmcm1.vivado.tcl",
    """create_ip -name clk_wiz -vendor xilinx.com -library ip -module_name vcu118_sys_clock_mmcm1 -dir $ipdir -force
    set_property -dict [list \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_SOURCE {No_buffer} \
    CONFIG.CLKOUT2_USED {true} \
    CONFIG.CLKOUT3_USED {false} \
    CONFIG.CLKOUT4_USED {false} \
    CONFIG.CLKOUT5_USED {false} \
    CONFIG.CLKOUT6_USED {false} \
    CONFIG.CLKOUT7_USED {false} \
    CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {32.5} \
    CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {65} \
    CONFIG.CLK_IN1_BOARD_INTERFACE {Custom} \
    CONFIG.PRIM_IN_FREQ {250.000} \
    CONFIG.CLKIN1_JITTER_PS {50.0} \
    CONFIG.MMCM_DIVCLK_DIVIDE {25} \
    CONFIG.MMCM_CLKFBOUT_MULT_F {117.000} \
    CONFIG.MMCM_CLKIN1_PERIOD {4.000} \
    CONFIG.MMCM_CLKOUT0_DIVIDE_F {36.000} \
    CONFIG.MMCM_CLKOUT1_DIVIDE {18} \
    CONFIG.MMCM_CLKOUT2_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT3_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT4_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT5_DIVIDE {1} \
    CONFIG.MMCM_CLKOUT6_DIVIDE {1} \
    CONFIG.NUM_OUT_CLKS {2} \
    CONFIG.CLKOUT1_JITTER {257.594} \
    CONFIG.CLKOUT1_PHASE_ERROR {366.693} \
    CONFIG.CLKOUT2_JITTER {232.023} \
    CONFIG.CLKOUT2_PHASE_ERROR {366.693}] \
    [get_ips vcu118_sys_clock_mmcm1] """
  )
}

//-------------------------------------------------------------------------
// vcu118reset
//-------------------------------------------------------------------------

class vcu118reset() extends BlackBox
{
  val io = new Bundle{
    val areset = Bool(INPUT)
    val clock1 = Clock(INPUT)
    val reset1 = Bool(OUTPUT)
    val clock2 = Clock(INPUT)
    val reset2 = Bool(OUTPUT)
    val clock3 = Clock(INPUT)
    val reset3 = Bool(OUTPUT)
    val clock4 = Clock(INPUT)
    val reset4 = Bool(OUTPUT)
  }
}

//-------------------------------------------------------------------------
// sdio_spi_bridge
//-------------------------------------------------------------------------

class sdio_spi_bridge() extends BlackBox
{
  val io = new Bundle{
    val clk      = Clock(INPUT)
    val reset    = Bool(INPUT)
    val sd_cmd   = Analog(1.W)
    val sd_dat   = Analog(4.W)
    val spi_sck  = Bool(INPUT)
    val spi_cs   = Bool(INPUT)
    val spi_dq_o = Bits(INPUT,4)
    val spi_dq_i = Bits(OUTPUT,4)
  }
}
