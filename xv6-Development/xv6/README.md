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