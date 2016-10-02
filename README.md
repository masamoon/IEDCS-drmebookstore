# IEDCS-drmebookstore
Ebook store using DRM

Ebook store that uses DRM.

A user key is generated ,unique to each user, a player key is tied to the client, a device key is tied to the machine where the client is installed
(using H/W unique fingerprints like HDD serial number and MAC address).

When an ebook is bought it is ciphered using a file key, unique to each ebook. This file key is then ciphered using the three keys described above, the result
of this triple cipher is a ciphertext stored in the ebook header.
When the client wants to read an ebook and display it to the user, it extracts the ciphertext from the header, deciphers it with the player key, sends
the result to the server which then deciphers it with the user key, this result is sent, once again, to the client that uses the device key for
the final step in the deciphering. The result of this triple decipher is the file key which is then used to decipher the ebook itself.
The client is also validated using RSA.

There is also a component of authentication using the portuguese id card for the creation of new users and to login into the store.

Thanks to (https://github.com/mrpdaemon/encfs-java) for the clean encfs java API used to cipher the ebooks on the server side.
