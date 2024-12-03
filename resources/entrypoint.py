import argparse
import subprocess

tsa_command = ["java", "-jar", "/home/tsa.jar"]
tsa_safety_properties_command = ["java", "-jar", "/home/tsa-safety-properties.jar"]
# tlb_parser_command = ["/ton/build/crypto/tlbc", "-j"]
tlb_parser_command = ["/home/tlbc", "-j", "/home/block.tlb"]
tlb_json_output_name = "/home/tlb.json"


class TlbPath:
    pass

parser = argparse.ArgumentParser(prog='ton-analysis', add_help=False)
parser.add_argument('-c', '--safety_properties_checker', action='store_true', help='Run safety properties checker (instead of general analysis)')

tlb_path_namespace = TlbPath()
parser.add_argument('-t', '--tlb', help='The path to the file containing the TL-B scheme for the entrypoint')

wrapper_args, analysis_args = parser.parse_known_args(namespace=tlb_path_namespace)
tsa_command_with_args = tsa_command + analysis_args

if wrapper_args.tlb is not None:
    tlb_parser_command_with_input_and_output = tlb_parser_command + [tlb_path_namespace.tlb]
    with open(tlb_json_output_name, 'w') as tlb_json_output_file:
        subprocess.run(tlb_parser_command_with_input_and_output, stdout=tlb_json_output_file)
    tsa_command_with_args += ["--tlb", tlb_json_output_name]

if wrapper_args.safety_properties_checker:
    subprocess.run(tsa_safety_properties_command + analysis_args)
else:
    subprocess.run(tsa_command_with_args)
