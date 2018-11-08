package com.twitter.finagle

import com.twitter.finagle.dispatch.SerialServerDispatcher
import com.twitter.finagle.netty4.Netty4Listener
import com.twitter.finagle.server.{Listener, StackServer, StdStackServer}
import com.twitter.finagle.transport.{Transport, TransportContext}
import com.twitter.util.Closable


object Serial {

  class Server[Req <: String, Res <: String](
                                              val stack: Stack[ServiceFactory[Req, Res]] = StackServer.newStack[Req, Res],
                                              val params: Stack.Params = StackServer.defaultParams
                                            )(
                                              implicit mIn: Manifest[Res], mOut: Manifest[Req]
                                            ) extends StdStackServer[Req, Res, Server[Req, Res]] {
    override type In = Res
    override type Out = Req
    override type Context = TransportContext

    override protected def newListener(): Listener[In, Out, Context] =
      Netty4Listener[In, Out](Pipeline, params)

    override protected def copy1(
                                  stack: Stack[ServiceFactory[Req, Res]] = this.stack,
                                  params: Stack.Params = this.params
                                ): Server[Req, Res] = new Server(stack, params)

    override protected def newDispatcher(transport: Transport[Res, Req] {
      type Context <: TransportContext
    }, service: Service[Req, Res]): Closable =
      new SerialServerDispatcher(transport, service)
  }

  def server[Req <: String, Res <: String]()(implicit mIn: Manifest[Res], mOut: Manifest[Req]): Server[Req, Res] =
    new Server[Req, Res]()
}

