# UNICORE XUUDB

The UNICORE XUUDB is a service acting as an 'attribute source' as part
of a UNICORE installation. It is an optional service, that is best
suited a per-site service, providing attributes for multiple
UNICORE/X-like services at a site.

The XUUDB maps a UNICORE user identity (an X.500
distinguished name (DN)) to a set of attributes. The attributes are
typically used to provide local account details (uid, gid(s)) and
commonly also to provide authorization information, i.e. the
user's role.
