/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

public class HeaderSubstitutor {
	IncludeMap[] INCLUDE_MAPS = {
			cIncludeMap, cIncludeMapWeak, cppIncludeMap, cppIncludeMapWeak
		};

	@SuppressWarnings("nls")
	private static final IncludeMap symbolIncludeMap = new IncludeMap(false, false, new String[] {
			"EOF", "<stdio.h>",
			"EOF", "<libio.h>",
			"NULL", "<stddef.h>",
			"NULL", "<cstddef>",
			"NULL", "<cstdio>",
			"NULL", "<cstdlib>",
			"NULL", "<cstring>",
			"NULL", "<ctime>",
			"NULL", "<cwchar>",
			"NULL", "<locale.h>",
			"NULL", "<stdio.h>",
			"NULL", "<stdlib.h>",
			"NULL", "<string.h>",
			"NULL", "<time.h>",
			"NULL", "<wchar.h>",
			"blkcnt_t", "<sys/stat.h>",
			"blkcnt_t", "<sys/types.h>",
			"blksize_t", "<sys/types.h>",
			"blksize_t", "<sys/stat.h>",
			"calloc", "<stdlib.h>",
			"daddr_t", "<sys/types.h>",
			"daddr_t", "<rpc/types.h>",
			"dev_t", "<sys/types.h>",
			"dev_t", "<sys/stat.h>",
			"error_t", "<errno.h>",
			"error_t", "<argp.h>",
			"error_t", "<argz.h>",
			"free", "<stdlib.h>",
			"fsblkcnt_t", "<sys/types.h>",
			"fsblkcnt_t", "<sys/statvfs.h>",
			"fsfilcnt_t", "<sys/types.h>",
			"fsfilcnt_t", "<sys/statvfs.h>",
			"gid_t", "<sys/types.h>",
			"gid_t", "<grp.h>",
			"gid_t", "<pwd.h>",
			"gid_t", "<stropts.h>",
			"gid_t", "<sys/ipc.h>",
			"gid_t", "<sys/stat.h>",
			"gid_t", "<unistd.h>",
			"id_t", "<sys/types.h>",
			"id_t", "<sys/resource.h>",
			"ino64_t", "<sys/types.h>",
			"ino64_t", "<dirent.h>",
			"ino_t", "<sys/types.h>",
			"ino_t", "<dirent.h>",
			"ino_t", "<sys/stat.h>",
			"int8_t", "<sys/types.h>",
			"int8_t", "<stdint.h>",
			"intptr_t", "<stdint.h>",
			"intptr_t", "<unistd.h>",
			"key_t", "<sys/types.h>",
			"key_t", "<sys/ipc.h>",
			"malloc", "<stdlib.h>",
			"mode_t", "<sys/types.h>",
			"mode_t", "<sys/stat.h>",
			"mode_t", "<sys/ipc.h>",
			"mode_t", "<sys/mman.h>",
			"nlink_t", "<sys/types.h>",
			"nlink_t", "<sys/stat.h>",
			"off64_t", "<sys/types.h>",
			"off64_t", "<unistd.h>",
			"off_t", "<sys/types.h>",
			"off_t", "<unistd.h>",
			"off_t", "<sys/stat.h>",
			"off_t", "<sys/mman.h>",
			"pid_t", "<sys/types.h>",
			"pid_t", "<unistd.h>",
			"pid_t", "<signal.h>",
			"pid_t", "<sys/msg.h>",
			"pid_t", "<sys/shm.h>",
			"pid_t", "<termios.h>",
			"pid_t", "<time.h>",
			"pid_t", "<utmpx.h>",
			"realloc", "<stdlib.h>",
			"sigset_t", "<signal.h>",
			"sigset_t", "<sys/epoll.h>",
			"sigset_t", "<sys/select.h>",
			"size_t", "<stddef.h>",
			"socklen_t", "<bits/socket.h>",
			"socklen_t", "<unistd.h>",
			"socklen_t", "<arpa/inet.h>",
			"ssize_t", "<sys/types.h>",
			"ssize_t", "<unistd.h>",
			"ssize_t", "<monetary.h>",
			"ssize_t", "<sys/msg.h>",
			"std::allocator", "<memory>",
			"std::allocator", "<string>",
			"std::allocator", "<vector>",
			"std::allocator", "<map>",
			"std::allocator", "<set>",
			"std::char_traits", "<string>",
			"std::char_traits", "<ostream>",
			"std::char_traits", "<istream>",
			"suseconds_t", "<sys/types.h>",
			"suseconds_t", "<sys/time.h>",
			"suseconds_t", "<sys/select.h>",
			"u_char", "<sys/types.h>",
			"u_char", "<rpc/types.h>",
			"uid_t", "<sys/types.h>",
			"uid_t", "<unistd.h>",
			"uid_t", "<pwd.h>",
			"uid_t", "<signal.h>",
			"uid_t", "<stropts.h>",
			"uid_t", "<sys/ipc.h>",
			"uid_t", "<sys/stat.h>",
			"useconds_t", "<sys/types.h>",
			"useconds_t", "<unistd.h>",
			"va_list", "<stdarg.h>",
		});

	@SuppressWarnings("nls")
	private static final IncludeMap cIncludeMap = new IncludeMap(true, false, new String[] {
			"<asm/errno-base.h>", "<errno.h>",
			"<asm/errno.h>", "<errno.h>",
			"<asm/ioctls.h>", "<sys/ioctl.h>",
			"<asm/posix_types_32.h>", "<asm/posix_types.h>",
			"<asm/posix_types_64.h>", "<asm/posix_types.h>",
			"<asm/ptrace-abi.h>", "<asm/ptrace.h>",
			"<asm/socket.h>", "<sys/socket.h>",
			"<asm/unistd.h>", "<syscall.h>",
			"<asm/unistd_32.h>", "<syscall.h>",
			"<asm/unistd_64.h>", "<syscall.h>",
			"<bits/a.out.h>", "<a.out.h>",
			"<bits/byteswap.h>", "<byteswap.h>",
			"<bits/cmathcalls.h>", "<complex.h>",
			"<bits/confname.h>", "<unistd.h>",
			"<bits/dirent.h>", "<dirent.h>",
			"<bits/dlfcn.h>", "<dlfcn.h>",
			"<bits/elfclass.h>", "<link.h>",
			"<bits/endian.h>", "<endian.h>",
			"<bits/environments.h>", "<unistd.h>",
			"<bits/errno.h>", "<errno.h>",
			"<bits/error.h>", "<error.h>",
			"<bits/fcntl.h>", "<fcntl.h>",
			"<bits/fcntl2.h>", "<fcntl.h>",
			"<bits/fenv.h>", "<fenv.h>",
			"<bits/fenvinline.h>", "<fenv.h>",
			"<bits/huge_val.h>", "<math.h>",
			"<bits/huge_valf.h>", "<math.h>",
			"<bits/huge_vall.h>", "<math.h>",
			"<bits/in.h>", "<netinet/in.h>",
			"<bits/inf.h>", "<math.h>",
			"<bits/ioctl-types.h>", "<sys/ioctl.h>",
			"<bits/ioctls.h>", "<sys/ioctl.h>",
			"<bits/ipc.h>", "<sys/ipc.h>",
			"<bits/ipctypes.h>", "<sys/ipc.h>",
			"<bits/libio-ldbl.h>", "<libio.h>",
			"<bits/link.h>", "<link.h>",
			"<bits/locale.h>", "<locale.h>",
			"<bits/mathcalls.h>", "<math.h>",
			"<bits/mathdef.h>", "<math.h>",
			"<bits/mathinline.h>", "<math.h>",
			"<bits/mman.h>", "<sys/mman.h>",
			"<bits/monetary-ldbl.h>", "<monetary.h>",
			"<bits/mqueue.h>", "<mqueue.h>",
			"<bits/mqueue2.h>", "<mqueue.h>",
			"<bits/msq.h>", "<sys/msg.h>",
			"<bits/nan.h>", "<math.h>",
			"<bits/netdb.h>", "<netdb.h>",
			"<bits/poll.h>", "<poll.h>",
			"<bits/posix1_lim.h>", "<limits.h>",
			"<bits/posix2_lim.h>", "<limits.h>",
			"<bits/posix_opt.h>", "<unistd.h>",
			"<bits/predefs.h>", "<features.h>",
			"<bits/printf-ldbl.h>", "<printf.h>",
			"<bits/pthreadtypes.h>", "<pthread.h>",
			"<bits/resource.h>", "<sys/resource.h>",
			"<bits/sched.h>", "<sched.h>",
			"<bits/select.h>", "<sys/select.h>",
			"<bits/sem.h>", "<sys/sem.h>",
			"<bits/semaphore.h>", "<semaphore.h>",
			"<bits/setjmp.h>", "<setjmp.h>",
			"<bits/shm.h>", "<sys/shm.h>",
			"<bits/sigaction.h>", "<signal.h>",
			"<bits/sigcontext.h>", "<signal.h>",
			"<bits/siginfo.h>", "<signal.h>",
			"<bits/signum.h>", "<signal.h>",
			"<bits/sigset.h>", "<signal.h>",
			"<bits/sigstack.h>", "<signal.h>",
			"<bits/sigthread.h>", "<signal.h>",
			"<bits/sockaddr.h>", "<sys/un.h>",
			"<bits/socket.h>", "<sys/socket.h>",
			"<bits/stab.def>", "<stab.h>",
			"<bits/stat.h>", "<sys/stat.h>",
			"<bits/statfs.h>", "<sys/statfs.h>",
			"<bits/statvfs.h>", "<sys/statvfs.h>",
			"<bits/stdio-ldbl.h>", "<stdio.h>",
			"<bits/stdio-lock.h>", "<libio.h>",
			"<bits/stdio.h>", "<stdio.h>",
			"<bits/stdio2.h>", "<stdio.h>",
			"<bits/stdio_lim.h>", "<stdio.h>",
			"<bits/stdlib-ldbl.h>", "<stdlib.h>",
			"<bits/stdlib.h>", "<stdlib.h>",
			"<bits/string.h>", "<string.h>",
			"<bits/string2.h>", "<string.h>",
			"<bits/string3.h>", "<string.h>",
			"<bits/stropts.h>", "<stropts.h>",
			"<bits/sys_errlist.h>", "<stdio.h>",
			"<bits/syscall.h>", "<syscall.h>",
			"<bits/syslog-ldbl.h>", "<syslog.h>",
			"<bits/syslog-path.h>", "<syslog.h>",
			"<bits/syslog.h>", "<syslog.h>",
			"<bits/termios.h>", "<termios.h>",
			"<bits/time.h>", "<sys/time.h>",
			"<bits/types.h>", "<sys/types.h>",
			"<bits/uio.h>", "<sys/uio.h>",
			"<bits/unistd.h>", "<unistd.h>",
			"<bits/ustat.h>", "<ustat.h>",
			"<bits/utmp.h>", "<utmp.h>",
			"<bits/utmpx.h>", "<utmpx.h>",
			"<bits/utsname.h>", "<sys/utsname.h>",
			"<bits/waitflags.h>", "<sys/wait.h>",
			"<bits/waitstatus.h>", "<sys/wait.h>",
			"<bits/wchar-ldbl.h>", "<wchar.h>",
			"<bits/wchar.h>", "<wchar.h>",
			"<bits/wchar2.h>", "<wchar.h>",
			"<bits/xopen_lim.h>", "<limits.h>",
			"<bits/xtitypes.h>", "<stropts.h>",
			"<linux/errno.h>", "<errno.h>",
			"<linux/limits.h>", "<limits.h>",
			"<linux/socket.h>", "<sys/socket.h>",
			"<sys/poll.h>", "<poll.h>",
			"<sys/syscall.h>", "<syscall.h>",
			"<sys/syslog.h>", "<syslog.h>",
			"<sys/ucontext.h>", "<ucontext.h>",
			"<sys/ustat.h>", "<ustat.h>",
			"<wait.h>", "<sys/wait.h>",
		});

	private static final IncludeMap cIncludeMapWeak = new IncludeMap(false, false, new String[] {
		});

	@SuppressWarnings("nls")
	private static final IncludeMap cppIncludeMap = new IncludeMap(true, true, new String[] {
			"<auto_ptr.h>", "<memory>",
			"<backward/auto_ptr.h>", "<memory>",
			"<backward/binders.h>", "<functional>",
			"<backward/hash_fun.h>", "<hash_map>",
			"<backward/hash_fun.h>", "<hash_set>",
			"<backward/hashtable.h>", "<hash_map>",
			"<backward/hashtable.h>", "<hash_set>",
			"<backward/strstream>", "<strstream>",
			"<binders.h>", "<functional>",
			"<bits/algorithmfwd.h>", "<algorithm>",
			"<bits/allocator.h>", "<memory>",
			"<bits/atomic_word.h>", "<ext/atomicity.h>",
			"<bits/basic_file.h>", "<fstream>",
			"<bits/basic_ios.h>", "<ios>",
			"<bits/basic_string.h>", "<string>",
			"<bits/basic_string.tcc>", "<string>",
			"<bits/boost_concept_check.h>", "<bits/concept_check.h>",
			"<bits/boost_sp_shared_count.h>", "<memory>",
			"<bits/c++allocator.h>", "<memory>",
			"<bits/c++config.h>", "<cstddef>",
			"<bits/c++io.h>", "<ext/stdio_sync_filebuf.h>",
			"<bits/char_traits.h>", "<string>",
			"<bits/cmath.tcc>", "<cmath>",
			"<bits/codecvt.h>", "<fstream>",
			"<bits/codecvt.h>", "<locale>",
			"<bits/ctype_base.h>", "<locale>",
			"<bits/ctype_base.h>", "<ios>",
			"<bits/ctype_inline.h>", "<locale>",
			"<bits/ctype_inline.h>", "<ios>",
			"<bits/cxxabi_tweaks.h>", "<cxxabi.h>",
			"<bits/deque.tcc>", "<deque>",
			"<bits/exception_defines.h>", "<exception>",
			"<bits/fstream.tcc>", "<fstream>",
			"<bits/functexcept.h>", "<algorithm>",
			"<bits/functional_hash.h>", "<unordered_map>",
			"<bits/gslice.h>", "<valarray>",
			"<bits/gslice_array.h>", "<valarray>",
			"<bits/hashtable.h>", "<unordered_map>",
			"<bits/hashtable.h>", "<unordered_set>",
			"<bits/indirect_array.h>", "<valarray>",
			"<bits/ios_base.h>", "<iostream>",
			"<bits/ios_base.h>", "<ios>",
			"<bits/ios_base.h>", "<iomanip>",
			"<bits/istream.tcc>", "<istream>",
			"<bits/list.tcc>", "<list>",
			"<bits/locale_classes.h>", "<locale>",
			"<bits/locale_classes.h>", "<ios>",
			"<bits/locale_classes.tcc>", "<locale>",
			"<bits/locale_classes.tcc>", "<ios>",
			"<bits/locale_facets.h>", "<locale>",
			"<bits/locale_facets.h>", "<ios>",
			"<bits/locale_facets.tcc>", "<locale>",
			"<bits/locale_facets.tcc>", "<ios>",
			"<bits/locale_facets_nonio.h>", "<locale>",
			"<bits/locale_facets_nonio.tcc>", "<locale>",
			"<bits/localefwd.h>", "<locale>",
			"<bits/mask_array.h>", "<valarray>",
			"<bits/messages_members.h>", "<locale>",
			"<bits/move.h>", "<algorithm>",
			"<bits/ostream.tcc>", "<ostream>",
			"<bits/ostream_insert.h>", "<ostream>",
			"<bits/postypes.h>", "<iostream>",
			"<bits/postypes.h>", "<string>",
			"<bits/slice_array.h>", "<valarray>",
			"<bits/sstream.tcc>", "<sstream>",
			"<bits/stl_algo.h>", "<algorithm>",
			"<bits/stl_algobase.h>", "<algorithm>",
			"<bits/stl_bvector.h>", "<vector>",
			"<bits/stl_construct.h>", "<memory>",
			"<bits/stl_deque.h>", "<deque>",
			"<bits/stl_function.h>", "<functional>",
			"<bits/stl_heap.h>", "<queue>",
			"<bits/stl_iterator.h>", "<iterator>",
			"<bits/stl_iterator_base_funcs.h>", "<iterator>",
			"<bits/stl_iterator_base_types.h>", "<iterator>",
			"<bits/stl_list.h>", "<list>",
			"<bits/stl_map.h>", "<map>",
			"<bits/stl_move.h>", "<algorithm>",
			"<bits/stl_multimap.h>", "<map>",
			"<bits/stl_multiset.h>", "<set>",
			"<bits/stl_numeric.h>", "<numeric>",
			"<bits/stl_pair.h>", "<utility>",
			"<bits/stl_pair.h>", "<tr1/utility>",
			"<bits/stl_queue.h>", "<queue>",
			"<bits/stl_raw_storage_iter.h>", "<memory>",
			"<bits/stl_relops.h>", "<utility>",
			"<bits/stl_set.h>", "<set>",
			"<bits/stl_stack.h>", "<stack>",
			"<bits/stl_tempbuf.h>", "<memory>",
			"<bits/stl_tree.h>", "<map>",
			"<bits/stl_tree.h>", "<set>",
			"<bits/stl_uninitialized.h>", "<memory>",
			"<bits/stl_vector.h>", "<vector>",
			"<bits/stream_iterator.h>", "<iterator>",
			"<bits/streambuf.tcc>", "<streambuf>",
			"<bits/streambuf_iterator.h>", "<iterator>",
			"<bits/streambuf_iterator.h>", "<ios>",
			"<bits/stringfwd.h>", "<string>",
			"<bits/valarray_after.h>", "<valarray>",
			"<bits/valarray_array.h>", "<valarray>",
			"<bits/valarray_array.tcc>", "<valarray>",
			"<bits/valarray_before.h>", "<valarray>",
			"<bits/vector.tcc>", "<vector>",
			"<debug/safe_iterator.tcc>", "<debug/safe_iterator.h>",
			"<exception_defines.h>", "<exception>",
			"<ext/algorithm>", "<algorithm>",
			"<ext/functional>", "<functional>",
			"<ext/hash_map>", "<hash_map>",
			"<ext/hash_set>", "<hash_set>",
			"<ext/numeric>", "<numeric>",
			"<ext/slist>", "<slist>",
			"<ext/sso_string_base.h>", "<string>",
			"<ext/vstring.h>", "<string>",
			"<ext/vstring.tcc>", "<string>",
			"<ext/vstring_fwd.h>", "<string>",
			"<hash_fun.h>", "<hash_map>",
			"<hash_fun.h>", "<hash_set>",
			"<hashtable.h>", "<hash_map>",
			"<hashtable.h>", "<hash_set>",
			"<tr1/bessel_function.tcc>", "<tr1/cmath>",
			"<tr1/beta_function.tcc>", "<tr1/cmath>",
			"<tr1/ell_integral.tcc>", "<tr1/cmath>",
			"<tr1/exp_integral.tcc>", "<tr1/cmath>",
			"<tr1/gamma.tcc>", "<tr1/cmath>",
			"<tr1/hypergeometric.tcc>", "<tr1/cmath>",
			"<tr1/legendre_function.tcc>", "<tr1/cmath>",
			"<tr1/modified_bessel_func.tcc>", "<tr1/cmath>",
			"<tr1/poly_hermite.tcc>", "<tr1/cmath>",
			"<tr1/poly_laguerre.tcc>", "<tr1/cmath>",
			"<tr1/riemann_zeta.tcc>", "<tr1/cmath>",
			"<tr1_impl/array>", "<array>",
			"<tr1_impl/array>", "<tr1/array>",
			"<tr1_impl/boost_shared_ptr.h>", "<memory>",
			"<tr1_impl/boost_shared_ptr.h>", "<tr1/memory>",
			"<tr1_impl/boost_sp_counted_base.h>", "<memory>",
			"<tr1_impl/boost_sp_counted_base.h>", "<tr1/memory>",
			"<tr1_impl/cctype>", "<cctype>",
			"<tr1_impl/cctype>", "<tr1/cctype>",
			"<tr1_impl/cfenv>", "<cfenv>",
			"<tr1_impl/cfenv>", "<tr1/cfenv>",
			"<tr1_impl/cinttypes>", "<cinttypes>",
			"<tr1_impl/cinttypes>", "<tr1/cinttypes>",
			"<tr1_impl/cmath>", "<cmath>",
			"<tr1_impl/cmath>", "<tr1/cmath>",
			"<tr1_impl/complex>", "<complex>",
			"<tr1_impl/complex>", "<tr1/complex>",
			"<tr1_impl/cstdint>", "<cstdint>",
			"<tr1_impl/cstdint>", "<tr1/cstdint>",
			"<tr1_impl/cstdio>", "<cstdio>",
			"<tr1_impl/cstdio>", "<tr1/cstdio>",
			"<tr1_impl/cstdlib>", "<cstdlib>",
			"<tr1_impl/cstdlib>", "<tr1/cstdlib>",
			"<tr1_impl/cwchar>", "<cwchar>",
			"<tr1_impl/cwchar>", "<tr1/cwchar>",
			"<tr1_impl/cwctype>", "<cwctype>",
			"<tr1_impl/cwctype>", "<tr1/cwctype>",
			"<tr1_impl/functional>", "<functional>",
			"<tr1_impl/functional>", "<tr1/functional>",
			"<tr1_impl/functional_hash.h>", "<tr1/functional_hash.h>",
			"<tr1_impl/hashtable>", "<tr1/hashtable.h>",
			"<tr1_impl/random.tcc>", "<random>",
			"<tr1_impl/random.tcc>", "<tr1/random>",
			"<tr1_impl/random>", "<random>",
			"<tr1_impl/random>", "<tr1/random>",
			"<tr1_impl/regex>", "<regex>",
			"<tr1_impl/regex>", "<tr1/regex>",
			"<tr1_impl/type_traits>", "<tr1/type_traits>",
			"<tr1_impl/type_traits>", "<type_traits>",
			"<tr1_impl/unordered_map>", "<tr1/unordered_map>",
			"<tr1_impl/unordered_map>", "<unordered_map>",
			"<tr1_impl/unordered_set>", "<tr1/unordered_set>",
			"<tr1_impl/unordered_set>", "<unordered_set>",
			"<tr1_impl/utility>", "<tr1/utility>",
			"<tr1_impl/utility>", "<utility>",
		});

	@SuppressWarnings("nls")
	private static final IncludeMap cppIncludeMapWeak = new IncludeMap(false, true, new String[] {
			"<assert.h>", "<cassert>",
			"<complex.h>", "<ccomplex>",
			"<ctype.h>", "<cctype>",
			"<errno.h>", "<cerrno>",
			"<fenv.h>", "<cfenv>",
			"<float.h>", "<cfloat>",
			"<inttypes.h>", "<cinttypes>",
			"<iso646.h>", "<ciso646>",
			"<limits.h>", "<climits>",
			"<locale.h>", "<clocale>",
			"<math.h>", "<cmath>",
			"<setjmp.h>", "<csetjmp>",
			"<signal.h>", "<csignal>",
			"<stdarg.h>", "<cstdarg>",
			"<stdbool.h>", "<cstdbool>",
			"<stddef.h>", "<cstddef>",
			"<stdint.h>", "<cstdint>",
			"<stdio.h>", "<cstdio>",
			"<stdlib.h>", "<cstdlib>",
			"<string.h>", "<cstring>",
			"<tgmath.h>", "<ctgmath>",
			"<time.h>", "<ctime>",
			"<wchar.h>", "<cwchar>",
			"<wctype.h>", "<cwctype>",
			"<ios>", "<iostream>",
			"<ios>", "<istream>",
			"<ios>", "<ostream>",
			"<iosfwd>", "<ios>",
			"<iosfwd>", "<streambuf>",
			"<istream>", "<iostream>",
			"<istream>", "<fstream>",
			"<istream>", "<sstream>",
			"<ostream>", "<iostream>",
			"<ostream>", "<fstream>",
			"<ostream>", "<istream>",
			"<ostream>", "<sstream>",
			"<streambuf>", "<ios>",
		});

	private final InclusionContext fContext;

	private final IncludeMap fIncludeMap;
	private final IncludeMap fIncludeMapWeak;
	private IncludeMap[] fIncludeMaps;

	public HeaderSubstitutor(InclusionContext context) {
		fContext = context;
		fIncludeMap = new IncludeMap(cIncludeMap);
		if (fContext.isCXXLanguage())
			fIncludeMap.addAllMappings(cppIncludeMap);
		fIncludeMapWeak = new IncludeMap(cIncludeMapWeak);
		if (fContext.isCXXLanguage())
			fIncludeMapWeak.addAllMappings(cppIncludeMapWeak);
	}

	public void addIncludeMap(IncludeMap map) {
		if (fIncludeMaps != null)
			throw new IllegalStateException("Modifications are not allowed after maps have been finalized"); //$NON-NLS-1$
		if (map.isCppOnly() && !fContext.isCXXLanguage())
			return;
		IncludeMap receiver = map.isForcedReplacement() ? fIncludeMap : fIncludeMapWeak;
		receiver.addAllMappings(map);
	}

	/**
	 * Selects the header file to be appear in the {@code #include} statement given the header file
	 * that needs to be included. Returns absolute path for the header if it can be uniquely
	 * determined, or {@code null} otherwise. The header is determined uniquely if there are no
	 * optional replacement headers for it, and there are no more that one forced replacement.
	 * @param path absolute path of the header to be included directly or indirectly
	 * @return absolute path of the header to be included directly
	 */
	public IPath getUniqueRepresentativeHeader(IPath path) {
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
		if (includeInfo == null)
			return null;
		IncludeMap[] maps = getAllIncludeMaps();
		for (IncludeMap map : maps) {
			if (map.isForcedReplacement()) {
				List<IncludeInfo> replacements = map.getMapping(includeInfo);
				if (replacements.size() == 1) {
					includeInfo = replacements.get(0);
				} else if (replacements.size() > 1) {
					return null;
				}
			}
		}
		for (IncludeMap map : maps) {
			if (!map.isForcedReplacement()) {
				if (!map.getMapping(includeInfo).isEmpty()) {
					return null;
				}
			}
		}
		return fContext.resolveInclude(includeInfo);
	}

	public IPath getPreferredRepresentativeHeader(IPath path) {
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
		if (includeInfo == null)
			return path;
		// TODO(sprigogin): Take symbolIncludeMap into account.
		List<IncludeInfo> candidates = new ArrayList<IncludeInfo>();
		candidates.add(includeInfo);
		IncludeMap[] maps = getAllIncludeMaps();
		for (IncludeMap map : maps) {
			for (int i = 0; i < candidates.size();) {
				IncludeInfo candidate = candidates.get(i);
				List<IncludeInfo> replacements = map.getMapping(candidate);
				int increment = 1;
				if (!replacements.isEmpty()) {
					if (map.isForcedReplacement()) {
						candidates.remove(i);
						increment = 0;
					}
					candidates.addAll(i, replacements);
					increment += replacements.size();
				}
				i += increment;
			}
		}
		IPath firstResolved = null;
		for (IncludeInfo candidate : candidates) {
			IPath header = fContext.resolveInclude(candidate);
			if (header != null) {
				if (fContext.isIncluded(header))
					return header;
				if (firstResolved == null)
					firstResolved = header;
			}
		}

		return firstResolved != null ? firstResolved : path;
	}

	/**
	 * Performs heuristic header substitution.
	 */
	public IPath getPreferredRepresentativeHeaderByHeuristic(InclusionRequest request) {
		Set<IIndexFile> indexFiles = request.getDeclaringFiles().keySet();
		String symbolName = request.getBinding().getName();
		ArrayDeque<IIndexFile> front = new ArrayDeque<IIndexFile>();
		HashSet<IIndexFile> processed = new HashSet<IIndexFile>();

		try {
			// Look for headers without an extension and a matching name.
			if (fContext.isCXXLanguage()) {
				front.addAll(indexFiles);
				processed.addAll(indexFiles);

				while (!front.isEmpty()) {
					IIndexFile file = front.remove();

					String path = IncludeUtil.getPath(file);

					if (!hasExtension(path) && getFilename(path).equalsIgnoreCase(symbolName)) {
						// A C++ header without an extension and with a name which matches the name
						// of the symbol which should be declared is a perfect candidate for inclusion.
						return IndexLocationFactory.getAbsolutePath(file.getLocation());
					}

					// Process the next level of the include hierarchy.
					IIndexInclude[] includes = fContext.getIndex().findIncludedBy(file, 0);
					for (IIndexInclude include : includes) {
						IIndexFile includer = include.getIncludedBy();
						if (!processed.contains(includer)) {
							front.add(includer);
							processed.add(includer);
						}
					}
				}
			}

			// Repeat the process, this time only looking for headers without an extension.
			front.clear();
			front.addAll(indexFiles);
			processed.clear();
			processed.addAll(indexFiles);

			while (!front.isEmpty()) {
				IIndexFile file = front.remove();

				String path = IncludeUtil.getPath(file);

				if (fContext.isCXXLanguage() && !hasExtension(path)) {
					// A C++ header without an extension is still a very good candidate for inclusion.
					return IndexLocationFactory.getAbsolutePath(file.getLocation());
				}

				// Process the next level of the include hierarchy.
				IIndexInclude[] includes = fContext.getIndex().findIncludedBy(file, 0);
				for (IIndexInclude include : includes) {
					IIndexFile includer = include.getIncludedBy();
					if (!processed.contains(includer)) {
						URI uri = includer.getLocation().getURI();
						if (IncludeUtil.isSource(includer, fContext.getProject()) || isWorkspaceFile(uri)) {
							return IndexLocationFactory.getAbsolutePath(file.getLocation());
						}
						front.add(includer);
						processed.add(includer);
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}

		return request.getCandidatePaths().iterator().next();
	}

	private IncludeMap[] getAllIncludeMaps() {
		if (fIncludeMaps == null) {
			fIncludeMap.transitivelyClose();
			fIncludeMapWeak.transitivelyClose();
			fIncludeMaps = new IncludeMap[] { fIncludeMap, fIncludeMapWeak };
		}
		return fIncludeMaps;
	}

	/**
	 * Returns whether the given URI points within the workspace.
	 */
	private static boolean isWorkspaceFile(URI uri) {
		for (IFile file : ResourceLookup.findFilesForLocationURI(uri)) {
			if (file.exists()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the given path has a file suffix, or not.
	 */
	private static boolean hasExtension(String path) {
		return path.indexOf('.', path.lastIndexOf('/') + 1) >= 0;
	}

	/**
	 * Returns the filename of the given path, without extension.
	 */
	private static String getFilename(String path) {
		int startPos = path.lastIndexOf('/') + 1;
		int endPos = path.lastIndexOf('.');
		if (endPos > startPos) {
			return path.substring(startPos, endPos);
		} else {
			return path.substring(startPos);
		}
	}
}
