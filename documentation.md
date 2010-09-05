---
title: Documentation
layout: default
---

This page contains documentation for the CCF library.

<a id="introductiontosynchronization"></a>
Introduction to synchronization in collaborative editing (DRAFT)
----------------------------------------------------------------

<p class="author">Aki Saarinen, 5.9.2010</p>

Synchronization between multiple parties in a collaborative
group-editing system is an interesting problem that can be described
for example as "the activity of coordinating the potentially
interfering actions of processes that operate in parallel" \[2\]. Popular
approaches for solving this problem include various locking schemes,
global serialization of actions by a single party (usually the server)
and finally what's called operational transformations (OT).

CCF is based on one of these OT algorithms, which allow users to make
instant changes to a local copy of the data, and then provide
mechanisms for eventually synchronizing all of these clients in such a
way, that intentions of all users are preserved whenever it's possible.
Conflict situations are usually merged automatically, even though user
interaction could be implemented when necessary.

### CCF and Jupiter

CCF is based on an algorithm called Jupiter. It was introduced by
Nichols et. al in the context of the Jupiter collaboration system
already in 1995 \[1\]. A variant of this same algorithm was used in the
<a href="http://wave.google.com">Google Wave</a> collaboration
platform. Even though various more advanced algorithms have been
developed since the introduction of Jupiter, the relative simplicity of
this algorithm still makes it a powerful tool even today.

### Limitations in CCF

CCF is suitable for synchronizing tree-like documents between multiple
clients using one central server. There are algorithms that work in a
pure peer-to-peer setting, but CCF won't.

### How it works? 

Let's use a simple example to take a look at how CCF works. Consider an
application that allows users to edit non-formatted basic text
collaboratively. Each user has a client running, which is then
connected to a central server over the Internet. The data structure of
this application can be represented as a flat list or characters, which
can also be considered as an ordered tree which has a fixed depth of 1.
Each action the user makes, is represented as an operation. For this
simple example, we have only two operations are available: insertion
and deletion. 

When a user writes a new character somewhere in the text, this will be
encoded as an insert operation. This insert contains the character that
is inserted, and the position where the user wanted to insert it, e.g.
<i>insert(4, 'a')</i>. Insert is applied to the local copy of the
user's document right away. Now, this operation along with information
of what was the document state when this operation was created (the
state is important, as we'll see), will be sent to the server. Upon
receiving this message, the server will decode it back to an operation
and state information. If nothing has happened concurrently, the server
will then apply this operation to its local copy of the document, and
then echo this same message to all other clients. Upon receiving,
clients also apply the operation and at this time, all clients are in a
synchronized state.

Now, let's consider a case where two users make a modification
simultaneously. First part is still similar, both operations get
encoded as an operation and will be applied to the local copies of the
users, after which operations along with their state information is
sent to the server. Now the magic starts to happen. One of these
messages will reach the server first, which means that it gets
processed exactly the same way as in our first example, because from
the server's point of view, nothing has happened concurrently (second
request has not yet reached the server). However, when the second
user's modification messagei reaches the server, it will notice that
the state at the time of creation of that operation is different from
server's current state. That's because of the first operation applied
by the first user. 

Now the server needs to <i>transform</i> the incoming operation in such
a way, that the <i>intentions of the user are preserved</i>. Let's say
the first operation was: <i>insert(4, 'a')</i> and the second operation
was <i>delete(8)</i>. After the insert has been applied, the given
index in the delete operation (8), is no longer valid. The insert to
position 4 has transformed all text behind it by one. Delete will be
transformed to <i>delete(9)</i> with help of a transformation function
and the state information. This transformed operation will now be
echoed to other clients.

Our second example considered a case where operations had happened in
the server before another operation reached the server. In this case,
the server needs to <i>transform</i> the incoming operation to preserve
its original intentions. This same thing can also happen in the client
side. Let's extend our previous example that the user whose operation
reached the server first, also inserted another character with
<i>insert(1, 'b')</i>, but that this operation didn't reach the server
before another client's delete. Now before echoed to this user, the
delete was transformed to <i>delete(9)</i> in the server. However, when
reaching the client, an <i>insert(1, 'b')</i> has been applied to the
client's local copy of the document. Incoming operation needs once
again to be transformed. We can detect this need from the state
information, and incoming delete will then be shifted once more to
<i>delete(10)</i>.

This simple scheme of transforming all incoming operations both in the
client and the server allows us to always apply any operation locally
right away, and upon receiving other clients' modifications, just
transform then appropriately.

### References

\[1\] Nichols, D. A., Curtis, P., Dixon, M., and Lamping, J. High-
latency, low-bandwidth windowing in the jupiter collaboration system.
In UIST '95: Proceedings of the 8th annual ACM symposium on User
interface and software technology (New York, NY, USA, 1995), ACM.

\[2\] Greenberg, S., and Marwood, D. Real time groupware as a dis-
tributed system: concurrency control and its effect on the interface. In
CSCW '94: Proceedings of the 1994 ACM conference on Computer supported
cooperative work (New York, NY, USA, 1994), ACM.