package com.twitter.finagle

import java.net.SocketAddress

import com.twitter.finagle.client.{StackClient, StdStackClient, Transporter}
import com.twitter.finagle.dispatch.{GenSerialClientDispatcher, PipeliningDispatcher, StalledPipelineTimeout}
import com.twitter.finagle.netty4.Netty4Transporter
import com.twitter.finagle.transport.{Transport, TransportContext}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.StorageUnit

/**
  * Created by a.m.shcherbakov on 05.10.17.
  */
object Parallel {

  class Client[Req <: String, Res <: String](
    val stack: Stack[ServiceFactory[Req, Res]] = StackClient.newStack[Req, Res],
    val params: Stack.Params = Stack.Params.empty
  )(
    implicit
    mIn: Manifest[Req], mOut: Manifest[Res]
  )
    extends StdStackClient[Req, Res, Client[Req, Res]]
    with param.WithSessionPool[Client[Req, Res]]
    with param.WithDefaultLoadBalancer[Client[Req, Res]] {

    protected type In = Req
    protected type Out = Res
    protected type Context = TransportContext

    protected override def copy1(
      stack: Stack[ServiceFactory[In, Out]] = this.stack, params: Stack.Params = this.params
    ): Client[Req, Res] = new Client(stack, params)

    protected override def newTransporter(addr: SocketAddress): Transporter[Req, Res, TransportContext] =
      Netty4Transporter.raw[In, Out](Pipeline, addr, params)

    protected override def newDispatcher(transport: Transport[In, Out] { type Context <: Client.this.Context }): Service[In, Out] = {
      val stalledTimeout = params[StalledPipelineTimeout]
      val stats = params[param.Stats].statsReceiver.scope(GenSerialClientDispatcher.StatsScope)
      new PipeliningDispatcher[In, Out](transport, stats, stalledTimeout.timeout, DefaultTimer.getInstance)
    }

  }

  def client[Req <: String, Res <: String]()(
    implicit
    mIn: Manifest[Req], mOut: Manifest[Res]
  ): Client[Req, Res] = new Client[Req, Res]

}
