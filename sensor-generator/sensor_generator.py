import random
from decimal import *
import uuid


def generate_next_reading( prev_reading ):
    getcontext.prec = 2
    change = Decimal( random.gauss( 0, 1.0 ) )
    out = min( prev_reading + change, 30.0 )
    out = max( out, -20.0 )
    return out


def generate_series( readings_count, series_begin_time=None ):
    if series_begin_time is None:
        series_begin_time = (datetime.datetime.now() - datetime.timedelta( seconds=5 * (readings_count - 1) ))
    timestamp = series_begin_time
    reading = Decimal( str( random.uniform( -20.0, 30.0 ) ) )

    readings_list.append( { reading, timestamp } )

    for i in range( 1, readings_count ):
        reading = generate_next_reading( reading )
        timestamp += datetime.timedelta( seconds=5 )

        readings_list.append( { reading, timestamp } )

    return { "readings": readings_list }

