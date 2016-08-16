package dma

import Chisel._
import rocket.RoCC
import uncore.tilelink._
import cde.{Parameters, Field}

case object CopyAccelShareMemChannel extends Field[Boolean]

class CopyAccelerator(implicit p: Parameters) extends RoCC()(p) {
  val ctrl = Module(new DmaController)
  val tracker = Module(new DmaTrackerFile)
  val memarb = Module(new ClientUncachedTileLinkIOArbiter(tracker.io.mem.size))

  ctrl.io.cmd <> io.cmd
  io.resp <> ctrl.io.resp
  io.ptw.head <> ctrl.io.ptw
  io.busy := ctrl.io.busy

  tracker.io.dma <> ctrl.io.dma
  memarb.io.in <> tracker.io.mem

  if (p(CopyAccelShareMemChannel)) {
    require(io.utl.size == 0)
    io.autl <> memarb.io.out
  } else {
    require(io.utl.size == 1)
    io.utl.head <> memarb.io.out
    io.autl.acquire.valid := Bool(false)
    io.autl.grant.ready := Bool(false)
  }
  io.mem.req.valid := Bool(false)
}
