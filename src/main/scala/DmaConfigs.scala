package rocketchip

import Chisel._
import dma._
import rocket._
import cde.{Parameters, Config, Knob, CDEMatchError}

class WithDma extends Config(
  topDefinitions = (pname, site, here) => pname match {
    case BuildRoCC => Seq(
      RoccParameters(
        opcodes = OpcodeSet.all,
        generator = (p: Parameters) => Module(new CopyAccelerator()(p)),
        nMemChannels = (if (site(CopyAccelShareMemChannel)) 0 else 1),
        nPTWPorts = 1))
    case CopyAccelShareMemChannel => Knob("CA_SHARE_MEM_CHANNEL")
    case NDmaTransactors => 1
    case NDmaXacts => 4
    // Each tracker uses 3 different client_xact_id
    case RoccMaxTaggedMemXacts => 3 * site(NDmaTransactors)
  },
  knobValues = {
    case "CA_SHARE_MEM_CHANNEL" => false
  })

class DmaConfig extends Config(new WithDma ++ new BaseConfig)