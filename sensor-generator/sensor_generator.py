import math
import optparse
import random
import datetime
from sys import exit
from dataclasses import dataclass
from time import sleep

import requests
from decimal import *


@dataclass
class Reading:
    value: Decimal
    timestamp: str

class SinusoidalGenerator:
    def __init__(self, period=20):
        self.step = 0
        self.period = period
        self.phase = None

    def reset( self ):
        self.phase = None

    def generate(self, previous_reading):
        amplitude = (max_val - min_val) / 2
        middle = (max_val + min_val) / 2

        if self.phase is None:
            normalized = float((previous_reading - Decimal(str(middle))) / Decimal(str(amplitude)))
            normalized = max(-1.0, min(1.0, normalized))
            self.phase = math.asin(normalized)

        self.phase += 2 * math.pi / self.period
        value = middle + amplitude * math.sin(self.phase)

        return Decimal(str(value))

def generate_gauss_reading(previous_reading):
    change = Decimal(str(random.gauss(0, 1.0)))
    return min(
        max(previous_reading + change, Decimal(str(min_val))),
        Decimal(str(max_val))
    )

def generate_random_reading(previous_reading):
    return Decimal(str(random.uniform(min_val, max_val)))

def generate_const_reading(previous_reading):
    return previous_reading

max_val = 30.0
min_val = -20.0
gen = SinusoidalGenerator()
generators = {
    "gauss": generate_gauss_reading,
    "random": generate_random_reading,
    "const": generate_const_reading,
    "sin": gen.generate
}

def generate_series( readings_count, readings_interval, rng, initial_value = None, series_begin_time: datetime.datetime | None = None ):
    if rng == "sin":
        gen.reset()
    if series_begin_time is None:
        series_begin_time = (datetime.datetime.now( datetime.timezone.utc ) - datetime.timedelta( seconds=readings_interval * (readings_count - 1) ))
    timestamp = series_begin_time
    reading = initial_value if initial_value is not None else Decimal( str( random.uniform( min_val, max_val ) ) )

    readings_list = [ Reading(
            value = reading,
            timestamp = timestamp.isoformat().replace( "+00:00", "Z" ) ) ]

    for i in range( 1, readings_count ):
        reading = generators.get( rng )( reading )
        timestamp += datetime.timedelta( seconds=readings_interval )

        readings_list.append( Reading(
            value = reading,
            timestamp = timestamp.isoformat().replace( "+00:00", "Z" ) ) )

    return readings_list

def generate_next_reading( rng, previous_value):
    timestamp = datetime.datetime.now( datetime.timezone.utc)
    reading = generators.get( rng )( previous_value )

    return Reading( value = reading, timestamp = timestamp.isoformat().replace( "+00:00", "Z" ) )

def send_dto_to_api( api_url, api_key, reading: Reading ):
    payload = {
        "value": reading.value,
        "timestamp": reading.timestamp
    }
    response = requests.post(
        f"{api_url}/api/measurements",
        headers={ "X-API-Key": api_key },
        json=payload
    )
    if response.ok:
        print( f"Measurement sent: {response.status_code}" )
    else:
        print( f"Measurement rejected: {response.status_code} - {response.text}" )

if __name__ == '__main__':
    parser = optparse.OptionParser()
    parser.add_option('-i','--interval', action="store", type="float",dest="interval", default=5, help="Odstęp czasu między losowo generowanymi odczytami" )
    parser.add_option('-c','--count', action="store", type="integer",dest="count", default=10, help="Ilość losowo wygenerowanych odczytów")
    parser.add_option('-t','--timestamp', type="string", default="", help="Czas pierwszego wygenerowanego odczytu")
    parser.add_option('-s','--starting_pos', type="float", default=0.0, help="Wartość pierwszego wygenerowanego odczytu")
    parser.add_option('--keep_sending', action="store_true", default=False, help="Flaga oznaczająca że skrypt będzie kontynuował wysyłanie nowych danych co interval")
    rng_choices = ["gauss","sin","random","const"]
    parser.add_option('--rng', action="store", type="string", dest="rng", default="gauss", choices=rng_choices, help="Sposób losowania następnego odczytu. Możliwe opcje %s" % rng_choices )

    (options, args) = parser.parse_args()
    interval = options.interval
    count = options.count
    initial_timestamp = None
    initial_timestamp = datetime.datetime.fromisoformat(
        options.timestamp.replace( "Z", "+00:00" ) ) if options.timestamp else None
    keep_sending = options.keep_sending
    rng_type = options.rng
    starting_pos = Decimal( str( options.starting_pos ) )

    if not min_val <= starting_pos <= max_val:
        exit( f"STARTING_POS must be between {min_val} and {max_val}" )
    api_url = args[0]
    api_key = args[1]

    if api_key is "":
        exit("NO API_KEY")
    if api_url is "":
        exit("NO API_URL")

    if keep_sending:
        if count != 0:
            records = generate_series( count, interval, rng_type, starting_pos, initial_timestamp )
            for i in range( count ):
                send_dto_to_api( api_url, api_key, records[i] )
                sleep(interval)
        else:
            curr_record = Reading( value = starting_pos,
                                   timestamp = datetime.datetime.now( datetime.timezone.utc).isoformat().replace( "+00:00", "Z" ) )
            while True:
                    send_dto_to_api( api_url, api_key, curr_record )
                    curr_record = generate_next_reading( rng_type, curr_record.value )
                    sleep(interval)
    else:
        records = generate_series( count, interval, rng_type, starting_pos, initial_timestamp )
        for i in range( count ):
            send_dto_to_api( api_url, api_key, records[ i ] )


