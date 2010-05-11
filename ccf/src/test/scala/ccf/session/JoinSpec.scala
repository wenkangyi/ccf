package ccf.session

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.transport.Connection

object JoinSpec extends Specification with Mockito {
  val connection = mock[Connection]
  val version = Version(1, 2)
  val clientId = ClientId.randomId
  val existingChannelId = ChannelId.randomId
  val newChannelId = ChannelId.randomId
  val session = Session(connection, version, clientId, 0, Set(existingChannelId))
  "Join message" should {
    val joinRequest = JoinRequest(newChannelId)(session)
    val joinMessage = Join(newChannelId)
    connection.send(joinRequest) returns None
    "invoke #send with a message and return a valid new session" in {
      val (nextSession: Session, _) = joinMessage.send(session)
      nextSession.seqId must be equalTo(1)
      nextSession.channels must be equalTo(Set(existingChannelId, newChannelId))
      connection.send(joinRequest) was called
    }
    "take no action if already a member of a channel" in {
      val joinExistingMessage = Join(existingChannelId)
      val (nextSession: Session, _) = joinExistingMessage.send(session)
      nextSession must be equalTo(session)
      connection.send(joinRequest) wasnt called
    }
  }
}