/*============================================================================
  bandwidth, a benchmark to estimate memory transfer bandwidth.
  Copyright (C) 2005-2019 by Zack T Smith.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

  The author may be reached at 1 at zsmith dot co.
 *===========================================================================*/

#ifndef _ROUTINES_H
#define _ROUTINES_H

#include <stdint.h>

extern int Reader (void *ptr, unsigned long size, unsigned long loops);
extern int Reader_nontemporal (void *ptr, unsigned long size, unsigned long loops);

extern int RandomReader (void *ptr, unsigned long n_chunks, unsigned long loops);

extern int Writer (void *ptr, unsigned long size, unsigned long loops, unsigned long value);
extern int Writer_nontemporal (void *ptr, unsigned long size, unsigned long loops, unsigned long value);
extern int RandomWriter (void *ptr, unsigned long size, unsigned long loops, unsigned long value);

extern int ReaderVector (void *ptr, unsigned long size, unsigned long loops);
extern int RandomReaderVector (void *ptr, unsigned long n_chunks, unsigned long loops);
extern int WriterVector (void *ptr, unsigned long size, unsigned long loops, unsigned long value);
extern int RandomWriterVector (void *ptr, unsigned long size, unsigned long loops, unsigned long value);

#endif