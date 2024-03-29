# xv6 Socket API  
  
Socket Application Programming Interface (API) implementation in [xv6 Operating System of MIT](https://github.com/mit-pdos/xv6-public) which extends its functionality
to support socket programming via localhost networking.  
  
System calls (added):
------------------------------- 
  - **listen (localPort)**  
    This is for a server to listen to a local port for accepting client socket. Returns 0 for success, negative for failure.
    
  - **connect (remotePort, hostname)**  
    This is for a client to connect to remote port and host. **Only "localhost" and "127.0.0.1" as host**
    should be supported. Returns the local port number, if connection was successful. Negative
    values returned indicate failure.
    
  - **send (localPort, buffer, length)**  
    Sends a buffer to remote host connected to given localPort. Returns 0 for success. Negative values for failure. send() blocks, if remote host has not yet read earlier
    data
  
  - **recv (localPort, buffer, length)**  
    Receives data from remote host connected to given localPort. Returns 0 for success. Negative values for failure. recv() blocks, if no data is available.
  
  - **disconnect (localPort)**  
    Disconnects (and closes) the socket at given port number.
  
  
Limitations :
--------------------------------
- a server can connect to only one client at a time
- send() or recv() will block the caller process, until the recipients buffer is ready.
- timeout cannot be specified in the listen() or recv() call

Demonstration:
--------------------------------
- run this xv6 repo in qemu and a user program will show up in `socktest`  
	> make qemu
- in xv6 shell	
	> ls
	
	> socktest
	
`socktest` user program demonstrates a simple client-server interaction where client takes user input from console and server echoes it back to client. If any error occurs corresponding error codes is returned.  

Error codes:
-------------------------------
_`E_NOTFOUND -1025`  
`E_ACCESS_DENIED -1026`  
`E_WRONG_STATE -1027`  
`E_FAIL -1028`  
`E_INVALID_ARG -1029`  
`E_CONNECTION_RESET_BY_REMOTE -1030`_

Parameter issues would return `E_INVALID_ARG`.  
Accessing a socket that is not in the stable then `E_NOTFOUND`.  
Accessing a socket from wrong process then `E_ACCESS_DENIED`.  
Attempts to send or receive, when the socket is not connected, then
`E_WRONG_STATE`.  
If no more socket can be opened (limit exceeded) `E_FAIL`.  
If connection is reset during send or receive then `E_CONNECTION_RESET_BY_REMOTE`
  
*Error codes are defined in "signalcodes.h"*
  
This xv6 networking extension has been performed as an assignment of Operating System Sessional course in Level-3, Term-2 of Department of CSE, BUET.

# xv6 Paging Scheme

1.1.fork() : 
 - copies page table  
  
     
1.2.exec() : 
 - allocates (2) pages for loading prog into memory    
 - allocates 2 more pages & makes 1st page inaccessible  
	
  
   		   
2.1.malloc() :
 - calls ucore() which calls sbrk()
 - allocates at least 7+asked pages
 
2.2.sbrk() :
 - calls growproc() which calls allocuvm() 

2.3.free() :
 - frees in user space & calls nothing ??? donno how it frees  
    pages are never freed in xv6.     
 

3.1.`starting process` :
 - fork()      [cloned 4kB from "sh" not allocated in page]
 - sys_sbrk()  [already has size = 4kB ]
 - growproc()  [called with n    = 32kB]  
 - allocproc() [allocates PTE for  32kB]
 - exec()      [calls allocproc() with 4kB with parent && 
                maps 0->4kB & calls loaduvm() & copyout() & freeuvm() but **sets size = 8kB**]
 - loaduvm()   [maps code into new pgdir]
 - copyout()   [maps the stack into new pgdir]
 - freeuvm()   [frees the old allocated pgdir hence size reduces]               