package org.kjkoster.wedo.ble112;

import static java.lang.String.format;
import static java.lang.System.out;
import static org.kjkoster.wedo.ble112.HexDump.hexDump;

import org.thingml.bglib.BDAddr;
import org.thingml.bglib.BGAPIListener;

/**
 * A BGAPI listener that tries to log all packets in a readable fashion, making
 * it easy to trace all the incoming packets.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ProtocolLogger implements BGAPIListener {

    /**
     * Map the result onto a meaningful error code, see
     * http://www.silabs.com/documents/login/reference-manuals/Bluetooth_Smart_Software-BLE-1.3-API-RM.pdf
     */
    private String reasonOrResult(final int reasonOrResult) {
        switch (reasonOrResult) {
        case 0x0000:
            return "ok";
        case 0x0180:
            return "invalid parameter";
        case 0x0181:
            return "device in wrong state";
        case 0x0182:
            return "out of memory";
        case 0x0183:
            return "feature not implemented";
        case 0x0184:
            return "command not recognized";
        case 0x0185:
            return "timeout";
        case 0x0186:
            return "not connected";
        case 0x0187:
            return "flow";
        case 0x0188:
            return "user attribute";
        case 0x0189:
            return "invalid license key";
        case 0x018a:
            return "command too long";
        case 0x018b:
            return "out of bonds";
        case 0x0205:
            return "authentication failure";
        case 0x0206:
            return "pin or key missing";
        case 0x0207:
            return "memory capacity exceeded";
        case 0x0208:
            return "connection timeout";
        case 0x0209:
            return "connection limit exceeded";
        case 0x020c:
            return "command disallowed";
        case 0x0212:
            return "invalid command parameters";
        case 0x0213:
            return "remote user terminated connection";
        case 0x0216:
            return "connection terminated by local host";
        case 0x0222:
            return "ll response timeout";
        case 0x0228:
            return "ll instant passed";
        case 0x023a:
            return "controller busy";
        case 0x023b:
            return "unacceptable connection interval";
        case 0x023c:
            return "directed advertising timeout";
        case 0x023d:
            return "mic failure";
        case 0x023e:
            return "connection failed to be established";
        case 0x0301:
            return "passkey entry failed";
        case 0x0302:
            return "oob data is not available";
        case 0x0303:
            return "authentication requirements";
        case 0x0304:
            return "confirm value failed";
        case 0x0305:
            return "pairing not supported";
        case 0x0306:
            return "encryption key size";
        case 0x0307:
            return "command not supported";
        case 0x0308:
            return "unspecified reason";
        case 0x0309:
            return "repeated attempts";
        case 0x030a:
            return "invalid parameters";
        case 0x0401:
            return "invalid handle";
        case 0x0402:
            return "read not permitted";
        case 0x0403:
            return "write not permitted";
        case 0x0404:
            return "invalid pdu";
        case 0x0405:
            return "insufficient authentication";
        case 0x0406:
            return "request not supported";
        case 0x0407:
            return "invalid offset";
        case 0x0408:
            return "insufficient authorization";
        case 0x0409:
            return "prepare queue full";
        case 0x040a:
            return "attribute not found";
        case 0x040b:
            return "attribute not long";
        case 0x040c:
            return "insufficient encryption key size";
        case 0x040d:
            return "invalid attribute value length";
        case 0x040e:
            return "unlikely error";
        case 0x040f:
            return "insufficient encryption";
        case 0x0410:
            return "unsupported group type";
        case 0x0411:
            return "insufficient resources";
        case 0x0480:
            return "application error codes";
        default:
            return format("unknown reason or result 0x%04x", reasonOrResult);
        }
    }

    private String packetType(final int packet_type) {
        switch (packet_type) {
        case 0:
            return "connectable advertisement packet";
        case 2:
            return "non connectable advertisement packet";
        case 4:
            return "scan response packet";
        case 6:
            return "discoverable advertisement packet";
        default:
            return format("unknown packet_type 0x%02x", packet_type);
        }
    }

    private String addressType(final int address_type) {
        switch (address_type) {
        case 0:
            return "[0 gap_address_type_public]";
        case 1:
            return "[1 gap_address_type_random]";
        default:
            return format("[%d unknown address_type]", address_type);
        }
    }

    private String uuid(final byte[] uuid) {
        switch (uuid.length) {
        case 2:
            return format("[16-bit 0x%02x%02x%s]", uuid[1], uuid[0],
                    uuidDescription(uuid));
        case 4:
            return format("[32-bit 0x%02x%02x%02x%02x]", uuid[3], uuid[2],
                    uuid[1], uuid[0]);
        case 16:
            return format(
                    "[128-bit %02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x]",
                    uuid[15], uuid[14], uuid[13], uuid[12], uuid[11], uuid[10],
                    uuid[9], uuid[8], uuid[7], uuid[6], uuid[5], uuid[4],
                    uuid[3], uuid[2], uuid[1], uuid[0]);
        default:
            throw new IllegalArgumentException(
                    "bad uuid of " + uuid.length + " bytes");
        }
    }

    private String uuidDescription(final byte[] uuid) {
        // map only the really well known ones. Find more on
        // https://www.bluetooth.com/specifications/gatt
        switch ((uuid[1] << 8) + uuid[0]) {
        case 0x1800:
            return " org.bluetooth.service.generic_access";
        case 0x1801:
            return " org.bluetooth.service.generic_attribute";
        case 0x1802:
            return " org.bluetooth.service.immediate_alert";
        case 0x1803:
            return " org.bluetooth.service.link_loss";
        case 0x1804:
            return " org.bluetooth.service.tx_power";
        case 0x1805:
            return " org.bluetooth.service.current_time";
        case 0x1806:
            return " org.bluetooth.service.reference_time_update";
        case 0x1807:
            return " org.bluetooth.service.next_dst_change";
        case 0x1808:
            return " org.bluetooth.service.glucose";
        case 0x1809:
            return " org.bluetooth.service.health_thermometer";
        case 0x180a:
            return " org.bluetooth.service.device_information";
        case 0x180d:
            return " org.bluetooth.service.heart_rate";
        case 0x180e:
            return " org.bluetooth.service.phone_alert_status";
        case 0x180f:
            return " org.bluetooth.service.battery_service";
        case 0x1810:
            return " org.bluetooth.service.blood_pressure";
        case 0x1811:
            return " org.bluetooth.service.alert_notification";
        case 0x1812:
            return " org.bluetooth.service.human_interface_device";
        case 0x1813:
            return " org.bluetooth.service.scan_parameters";
        case 0x1814:
            return " org.bluetooth.service.running_speed_and_cadence";
        case 0x1815:
            return " org.bluetooth.service.automation_io";
        case 0x1816:
            return " org.bluetooth.service.cycling_speed_and_cadence";
        case 0x1818:
            return " org.bluetooth.service.cycling_power";
        case 0x1819:
            return " org.bluetooth.service.location_and_navigation";
        case 0x181a:
            return " org.bluetooth.service.environmental_sensing";
        case 0x181b:
            return " org.bluetooth.service.body_composition";
        case 0x181c:
            return " org.bluetooth.service.user_data";
        case 0x181d:
            return " org.bluetooth.service.weight_scale";
        case 0x181e:
            return " org.bluetooth.service.bond_management";
        case 0x181f:
            return " org.bluetooth.service.continuous_glucose_monitoring";
        case 0x1820:
            return " org.bluetooth.service.internet_protocol_support";
        case 0x1821:
            return " org.bluetooth.service.indoor_positioning";
        case 0x1822:
            return " org.bluetooth.service.pulse_oximeter";
        case 0x1823:
            return " org.bluetooth.service.http_proxy";
        case 0x1824:
            return " org.bluetooth.service.transport_discovery";
        case 0x1825:
            return " org.bluetooth.service.object_transfer";
        case 0x2800:
            return " org.bluetooth.attribute.gatt.primary_service_declaration";
        case 0x2801:
            return " org.bluetooth.attribute.gatt.secondary_service_declaration";
        case 0x2802:
            return " org.bluetooth.attribute.gatt.include_declaration";
        case 0x2803:
            return " org.bluetooth.attribute.gatt.characteristic_declaration";
        case 0x2900:
            return " org.bluetooth.descriptor.gatt.characteristic_extended_properties";
        case 0x2901:
            return " org.bluetooth.descriptor.gatt.characteristic_user_description";
        case 0x2902:
            return " org.bluetooth.descriptor.gatt.client_characteristic_configuration";
        case 0x2903:
            return " org.bluetooth.descriptor.gatt.server_characteristic_configuration";
        case 0x2904:
            return " org.bluetooth.descriptor.gatt.characteristic_presentation_format";
        case 0x2905:
            return " org.bluetooth.descriptor.gatt.characteristic_aggregate_format";
        case 0x2906:
            return " org.bluetooth.descriptor.valid_range";
        case 0x2907:
            return " org.bluetooth.descriptor.external_report_reference";
        case 0x2908:
            return " org.bluetooth.descriptor.report_reference";
        case 0x2909:
            return " org.bluetooth.descriptor.number_of_digitals";
        case 0x290a:
            return " org.bluetooth.descriptor.value_trigger_setting";
        case 0x290b:
            return " org.bluetooth.descriptor.es_configuration";
        case 0x290c:
            return " org.bluetooth.descriptor.es_measurement";
        case 0x290d:
            return " org.bluetooth.descriptor.es_trigger_setting";
        case 0x290e:
            return " org.bluetooth.descriptor.time_trigger_setting";
        case 0x2a00:
            return " org.bluetooth.characteristic.gap.device_name";
        case 0x2a01:
            return " org.bluetooth.characteristic.gap.appearance";
        case 0x2a02:
            return " org.bluetooth.characteristic.gap.peripheral_privacy_flag";
        case 0x2a03:
            return " org.bluetooth.characteristic.gap.reconnection_address";
        case 0x2a04:
            return " org.bluetooth.characteristic.gap.peripheral_preferred_connection_parameters";
        case 0x2a05:
            return " org.bluetooth.characteristic.gatt.service_changed";
        case 0x2a06:
            return " org.bluetooth.characteristic.alert_level";
        case 0x2a07:
            return " org.bluetooth.characteristic.tx_power_level";
        case 0x2a08:
            return " org.bluetooth.characteristic.date_time";
        case 0x2a09:
            return " org.bluetooth.characteristic.day_of_week";
        case 0x2a0a:
            return " org.bluetooth.characteristic.day_date_time";
        case 0x2a0c:
            return " org.bluetooth.characteristic.exact_time_256";
        case 0x2a0d:
            return " org.bluetooth.characteristic.dst_offset";
        case 0x2a0e:
            return " org.bluetooth.characteristic.time_zone";
        case 0x2a0f:
            return " org.bluetooth.characteristic.local_time_information";
        case 0x2a11:
            return " org.bluetooth.characteristic.time_with_dst";
        case 0x2a12:
            return " org.bluetooth.characteristic.time_accuracy";
        case 0x2a13:
            return " org.bluetooth.characteristic.time_source";
        case 0x2a14:
            return " org.bluetooth.characteristic.reference_time_information";
        case 0x2a16:
            return " org.bluetooth.characteristic.time_update_control_point";
        case 0x2a17:
            return " org.bluetooth.characteristic.time_update_state";
        case 0x2a18:
            return " org.bluetooth.characteristic.glucose_measurement";
        case 0x2a19:
            return " org.bluetooth.characteristic.battery_level";
        case 0x2a1c:
            return " org.bluetooth.characteristic.temperature_measurement";
        case 0x2a1d:
            return " org.bluetooth.characteristic.temperature_type";
        case 0x2a1e:
            return " org.bluetooth.characteristic.intermediate_temperature";
        case 0x2a21:
            return " org.bluetooth.characteristic.measurement_interval";
        case 0x2a22:
            return " org.bluetooth.characteristic.boot_keyboard_input_report";
        case 0x2a23:
            return " org.bluetooth.characteristic.system_id";
        case 0x2a24:
            return " org.bluetooth.characteristic.model_number_string";
        case 0x2a25:
            return " org.bluetooth.characteristic.serial_number_string";
        case 0x2a26:
            return " org.bluetooth.characteristic.firmware_revision_string";
        case 0x2a27:
            return " org.bluetooth.characteristic.hardware_revision_string";
        case 0x2a28:
            return " org.bluetooth.characteristic.software_revision_string";
        case 0x2a29:
            return " org.bluetooth.characteristic.manufacturer_name_string";
        case 0x2a2a:
            return " org.bluetooth.characteristic.ieee_11073-20601_regulatory_certification_data_list";
        case 0x2a2b:
            return " org.bluetooth.characteristic.current_time";
        case 0x2a2c:
            return " org.bluetooth.characteristic.magnetic_declination";
        case 0x2a31:
            return " org.bluetooth.characteristic.scan_refresh";
        case 0x2a32:
            return " org.bluetooth.characteristic.boot_keyboard_output_report";
        case 0x2a33:
            return " org.bluetooth.characteristic.boot_mouse_input_report";
        case 0x2a34:
            return " org.bluetooth.characteristic.glucose_measurement_context";
        case 0x2a35:
            return " org.bluetooth.characteristic.blood_pressure_measurement";
        case 0x2a36:
            return " org.bluetooth.characteristic.intermediate_cuff_pressure";
        case 0x2a37:
            return " org.bluetooth.characteristic.heart_rate_measurement";
        case 0x2a38:
            return " org.bluetooth.characteristic.body_sensor_location";
        case 0x2a39:
            return " org.bluetooth.characteristic.heart_rate_control_point";
        case 0x2a3f:
            return " org.bluetooth.characteristic.alert_status";
        case 0x2a40:
            return " org.bluetooth.characteristic.ringer_control_point";
        case 0x2a41:
            return " org.bluetooth.characteristic.ringer_setting";
        case 0x2a42:
            return " org.bluetooth.characteristic.alert_category_id_bit_mask";
        case 0x2a43:
            return " org.bluetooth.characteristic.alert_category_id";
        case 0x2a44:
            return " org.bluetooth.characteristic.alert_notification_control_point";
        case 0x2a45:
            return " org.bluetooth.characteristic.unread_alert_status";
        case 0x2a46:
            return " org.bluetooth.characteristic.new_alert";
        case 0x2a47:
            return " org.bluetooth.characteristic.supported_new_alert_category";
        case 0x2a48:
            return " org.bluetooth.characteristic.supported_unread_alert_category";
        case 0x2a49:
            return " org.bluetooth.characteristic.blood_pressure_feature";
        case 0x2a4a:
            return " org.bluetooth.characteristic.hid_information";
        case 0x2a4b:
            return " org.bluetooth.characteristic.report_map";
        case 0x2a4c:
            return " org.bluetooth.characteristic.hid_control_point";
        case 0x2a4d:
            return " org.bluetooth.characteristic.report";
        case 0x2a4e:
            return " org.bluetooth.characteristic.protocol_mode";
        case 0x2a4f:
            return " org.bluetooth.characteristic.scan_interval_window";
        case 0x2a50:
            return " org.bluetooth.characteristic.pnp_id";
        case 0x2a51:
            return " org.bluetooth.characteristic.glucose_feature";
        case 0x2a52:
            return " org.bluetooth.characteristic.record_access_control_point";
        case 0x2a53:
            return " org.bluetooth.characteristic.rsc_measurement";
        case 0x2a54:
            return " org.bluetooth.characteristic.rsc_feature";
        case 0x2a55:
            return " org.bluetooth.characteristic.sc_control_point";
        case 0x2a56:
            return " org.bluetooth.characteristic.digital";
        case 0x2a58:
            return " org.bluetooth.characteristic.analog";
        case 0x2a5a:
            return " org.bluetooth.characteristic.aggregate";
        case 0x2a5b:
            return " org.bluetooth.characteristic.csc_measurement";
        case 0x2a5c:
            return " org.bluetooth.characteristic.csc_feature";
        case 0x2a5e:
            return " org.bluetooth.characteristic.plx_spot_check_measurement";
        case 0x2a5f:
            return " org.bluetooth.characteristic.plx_continuous_measurement";
        case 0x2a60:
            return " org.bluetooth.characteristic.plx_features";
        case 0x2a63:
            return " org.bluetooth.characteristic.cycling_power_measurement";
        case 0x2a64:
            return " org.bluetooth.characteristic.cycling_power_vector";
        case 0x2a65:
            return " org.bluetooth.characteristic.cycling_power_feature";
        case 0x2a66:
            return " org.bluetooth.characteristic.cycling_power_control_point";
        case 0x2a67:
            return " org.bluetooth.characteristic.location_and_speed";
        case 0x2a68:
            return " org.bluetooth.characteristic.navigation";
        case 0x2a69:
            return " org.bluetooth.characteristic.position_quality";
        case 0x2a6a:
            return " org.bluetooth.characteristic.ln_feature";
        case 0x2a6b:
            return " org.bluetooth.characteristic.ln_control_point";
        case 0x2a6c:
            return " org.bluetooth.characteristic.elevation";
        case 0x2a6d:
            return " org.bluetooth.characteristic.pressure";
        case 0x2a6e:
            return " org.bluetooth.characteristic.temperature";
        case 0x2a6f:
            return " org.bluetooth.characteristic.humidity";
        case 0x2a70:
            return " org.bluetooth.characteristic.true_wind_speed";
        case 0x2a71:
            return " org.bluetooth.characteristic.true_wind_direction";
        case 0x2a72:
            return " org.bluetooth.characteristic.apparent_wind_speed";
        case 0x2a73:
            return " org.bluetooth.characteristic.apparent_wind_direction";
        case 0x2a74:
            return " org.bluetooth.characteristic.gust_factor";
        case 0x2a75:
            return " org.bluetooth.characteristic.pollen_concentration";
        case 0x2a76:
            return " org.bluetooth.characteristic.uv_index";
        case 0x2a77:
            return " org.bluetooth.characteristic.irradiance";
        case 0x2a78:
            return " org.bluetooth.characteristic.rainfall";
        case 0x2a79:
            return " org.bluetooth.characteristic.wind_chill";
        case 0x2a7a:
            return " org.bluetooth.characteristic.heat_index";
        case 0x2a7b:
            return " org.bluetooth.characteristic.dew_point";
        case 0x2a7d:
            return " org.bluetooth.characteristic.descriptor_value_changed";
        case 0x2a7e:
            return " org.bluetooth.characteristic.aerobic_heart_rate_lower_limit";
        case 0x2a7f:
            return " org.bluetooth.characteristic.aerobic_threshold";
        case 0x2a80:
            return " org.bluetooth.characteristic.age";
        case 0x2a81:
            return " org.bluetooth.characteristic.anaerobic_heart_rate_lower_limit";
        case 0x2a82:
            return " org.bluetooth.characteristic.anaerobic_heart_rate_upper_limit";
        case 0x2a83:
            return " org.bluetooth.characteristic.anaerobic_threshold";
        case 0x2a84:
            return " org.bluetooth.characteristic.aerobic_heart_rate_upper_limit";
        case 0x2a85:
            return " org.bluetooth.characteristic.date_of_birth";
        case 0x2a86:
            return " org.bluetooth.characteristic.date_of_threshold_assessment";
        case 0x2a87:
            return " org.bluetooth.characteristic.email_address";
        case 0x2a88:
            return " org.bluetooth.characteristic.fat_burn_heart_rate_lower_limit";
        case 0x2a89:
            return " org.bluetooth.characteristic.fat_burn_heart_rate_upper_limit";
        case 0x2a8a:
            return " org.bluetooth.characteristic.first_name";
        case 0x2a8b:
            return " org.bluetooth.characteristic.five_zone_heart_rate_limits";
        case 0x2a8c:
            return " org.bluetooth.characteristic.gender";
        case 0x2a8d:
            return " org.bluetooth.characteristic.heart_rate_max";
        case 0x2a8e:
            return " org.bluetooth.characteristic.height";
        case 0x2a8f:
            return " org.bluetooth.characteristic.hip_circumference";
        case 0x2a90:
            return " org.bluetooth.characteristic.last_name";
        case 0x2a91:
            return " org.bluetooth.characteristic.maximum_recommended_heart_rate";
        case 0x2a92:
            return " org.bluetooth.characteristic.resting_heart_rate";
        case 0x2a93:
            return " org.bluetooth.characteristic.sport_type_for_aerobic_and_anaerobic_thresholds";
        case 0x2a94:
            return " org.bluetooth.characteristic.three_zone_heart_rate_limits";
        case 0x2a95:
            return " org.bluetooth.characteristic.two_zone_heart_rate_limit";
        case 0x2a96:
            return " org.bluetooth.characteristic.vo2_max";
        case 0x2a97:
            return " org.bluetooth.characteristic.waist_circumference";
        case 0x2a98:
            return " org.bluetooth.characteristic.weight";
        case 0x2a99:
            return " org.bluetooth.characteristic.database_change_increment";
        case 0x2a9a:
            return " org.bluetooth.characteristic.user_index";
        case 0x2a9b:
            return " org.bluetooth.characteristic.body_composition_feature";
        case 0x2a9c:
            return " org.bluetooth.characteristic.body_composition_measurement";
        case 0x2a9d:
            return " org.bluetooth.characteristic.weight_measurement";
        case 0x2a9e:
            return " org.bluetooth.characteristic.weight_scale_feature";
        case 0x2a9f:
            return " org.bluetooth.characteristic.user_control_point";
        case 0x2aa0:
            return " org.bluetooth.characteristic.magnetic_flux_density_2d";
        case 0x2aa1:
            return " org.bluetooth.characteristic.magnetic_flux_density_3d";
        case 0x2aa2:
            return " org.bluetooth.characteristic.language";
        case 0x2aa3:
            return " org.bluetooth.characteristic.barometric_pressure_trend";
        case 0x2aa4:
            return " org.bluetooth.characteristic.bond_management_control_point";
        case 0x2aa5:
            return " org.bluetooth.characteristic.bond_management_feature";
        case 0x2aa6:
            return " org.bluetooth.characteristic.gap.central_address_resolution_support";
        case 0x2aa7:
            return " org.bluetooth.characteristic.cgm_measurement";
        case 0x2aa8:
            return " org.bluetooth.characteristic.cgm_feature";
        case 0x2aa9:
            return " org.bluetooth.characteristic.cgm_status";
        case 0x2aaa:
            return " org.bluetooth.characteristic.cgm_session_start_time";
        case 0x2aab:
            return " org.bluetooth.characteristic.cgm_session_run_time";
        case 0x2aac:
            return " org.bluetooth.characteristic.cgm_specific_ops_control_point";
        case 0x2aad:
            return " org.bluetooth.characteristic.indoor_positioning_configuration";
        case 0x2aae:
            return " org.bluetooth.characteristic.latitude";
        case 0x2aaf:
            return " org.bluetooth.characteristic.longitude";
        case 0x2ab0:
            return " org.bluetooth.characteristic.local_north_coordinate";
        case 0x2ab1:
            return " org.bluetooth.characteristic.local_east_coordinate";
        case 0x2ab2:
            return " org.bluetooth.characteristic.floor_number";
        case 0x2ab3:
            return " org.bluetooth.characteristic.altitude";
        case 0x2ab4:
            return " org.bluetooth.characteristic.uncertainty";
        case 0x2ab5:
            return " org.bluetooth.characteristic.location_name";
        case 0x2ab6:
            return " org.bluetooth.characteristic.uri";
        case 0x2ab7:
            return " org.bluetooth.characteristic.http_headers";
        case 0x2ab8:
            return " org.bluetooth.characteristic.http_status_code";
        case 0x2ab9:
            return " org.bluetooth.characteristic.http_entity_body";
        case 0x2aba:
            return " org.bluetooth.characteristic.http_control_point";
        case 0x2abb:
            return " org.bluetooth.characteristic.https_security";
        case 0x2abc:
            return " org.bluetooth.characteristic.tds_control_point";
        case 0x2abd:
            return " org.bluetooth.characteristic.ots_feature";
        case 0x2abe:
            return " org.bluetooth.characteristic.object_name";
        case 0x2abf:
            return " org.bluetooth.characteristic.object_type";
        case 0x2ac0:
            return " org.bluetooth.characteristic.object_size";
        case 0x2ac1:
            return " org.bluetooth.characteristic.object_first_created";
        case 0x2ac2:
            return " org.bluetooth.characteristic.object_last_modified";
        case 0x2ac3:
            return " org.bluetooth.characteristic.object_id";
        case 0x2ac4:
            return " org.bluetooth.characteristic.object_properties";
        case 0x2ac5:
            return " org.bluetooth.characteristic.object_action_control_point";
        case 0x2ac6:
            return " org.bluetooth.characteristic.object_list_control_point";
        case 0x2ac7:
            return " org.bluetooth.characteristic.object_list_filter";
        case 0x2ac8:
            return " org.bluetooth.characteristic.object_changed";
        default:
            return "";
        }
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_reset()
     */
    @Override
    public void receive_system_reset() {
        out.printf("SYSTEM: receive_system_reset()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_hello()
     */
    @Override
    public void receive_system_hello() {
        out.printf("SYSTEM: receive_system_hello()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_address_get(org.thingml.bglib.BDAddr)
     */
    @Override
    public void receive_system_address_get(BDAddr address) {
        out.printf("SYSTEM: receive_system_address_get(address: %s)\n",
                address);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_reg_write(int)
     */
    @Override
    public void receive_system_reg_write(int result) {
        out.printf("SYSTEM: receive_system_reg_write(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_reg_read(int, int)
     */
    @Override
    public void receive_system_reg_read(int address, int value) {
        out.printf("SYSTEM: receive_system_reg_read(addres: " + address
                + ", value: " + value + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_get_counters(int,
     *      int, int, int)
     */
    @Override
    public void receive_system_get_counters(int txok, int txretry, int rxok,
            int rxfail) {
        out.printf("SYSTEM: receive_system_get_counters(txok: " + txok
                + ", txretry: " + txretry + ", rxok: " + rxok + ", rxfail: "
                + rxfail + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_get_connections(int)
     */
    @Override
    public void receive_system_get_connections(int maxconn) {
        out.printf("SYSTEM: receive_system_get_connections(maxconn: " + maxconn
                + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_read_memory(int,
     *      byte[])
     */
    @Override
    public void receive_system_read_memory(int address, byte[] data) {
        out.printf(
                "SYSTEM: receive_system_read_memory(address: %d, data: %s)\n",
                address, hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_get_info(int, int,
     *      int, int, int, int, int)
     */
    @Override
    public void receive_system_get_info(int major, int minor, int patch,
            int build, int ll_version, int protocol_version, int hw) {
        out.printf("SYSTEM: receive_system_get_info(major.minor.patch-build: "
                + major + "." + minor + "." + patch + "-" + build
                + ", ll_version: " + ll_version + ", hw: " + hw + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_endpoint_tx()
     */
    @Override
    public void receive_system_endpoint_tx() {
        out.printf("SYSTEM: receive_system_endpoint_tx()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_whitelist_append(int)
     */
    @Override
    public void receive_system_whitelist_append(int result) {
        out.printf(
                "SYSTEM: receive_system_whitelist_append(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_whitelist_remove(int)
     */
    @Override
    public void receive_system_whitelist_remove(int result) {
        out.printf(
                "SYSTEM: receive_system_whitelist_remove(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_whitelist_clear()
     */
    @Override
    public void receive_system_whitelist_clear() {
        out.printf("SYSTEM: receive_system_whitelist_clear()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_boot(int, int, int,
     *      int, int, int, int)
     */
    @Override
    public void receive_system_boot(int major, int minor, int patch, int build,
            int ll_version, int protocol_version, int hw) {
        out.printf("SYSTEM: receive_system_boot(major.minor.patch: " + major
                + "." + minor + "." + patch + ", build: " + build
                + ", ll_version: " + ll_version + ", hw: " + hw + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_debug(byte[])
     */
    @Override
    public void receive_system_debug(byte[] data) {
        out.printf("SYSTEM: receive_system_debug(data: %s)\n", hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_endpoint_rx(int,
     *      byte[])
     */
    @Override
    public void receive_system_endpoint_rx(int endpoint, byte[] data) {
        out.printf(
                "SYSTEM: receive_system_endpoint_rx(endpoint: %s, data: %s)\n",
                endpoint, hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_defrag()
     */
    @Override
    public void receive_flash_ps_defrag() {
        out.printf("FLASH: receive_flash_ps_defrag()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_dump()
     */
    @Override
    public void receive_flash_ps_dump() {
        out.printf("FLASH: receive_flash_ps_dump()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_erase_all()
     */
    @Override
    public void receive_flash_ps_erase_all() {
        out.printf("FLASH: receive_flash_ps_erase_all()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_save(int)
     */
    @Override
    public void receive_flash_ps_save(int result) {
        out.printf("FLASH: receive_flash_ps_save(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_load(int, byte[])
     */
    @Override
    public void receive_flash_ps_load(int result, byte[] value) {
        out.printf(
                "FLASH: receive_flash_ps_load(result: [0x%04x %s], value: %s)\n",
                result, reasonOrResult(result), hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_erase()
     */
    @Override
    public void receive_flash_ps_erase() {
        out.printf("FLASH: receive_flash_ps_erase()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_erase_page(int)
     */
    @Override
    public void receive_flash_erase_page(int result) {
        out.printf("FLASH: receive_flash_erase_page(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_write_words()
     */
    @Override
    public void receive_flash_write_words() {
        out.printf("FLASH: receive_flash_write_words()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_flash_ps_key(int, byte[])
     */
    @Override
    public void receive_flash_ps_key(int key, byte[] value) {
        out.printf("FLASH: receive_flash_ps_key(key: %d], value: %s)\n", key,
                hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_write(int)
     */
    @Override
    public void receive_attributes_write(int result) {
        out.printf("ATT: receive_attributes_write(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_read(int, int,
     *      int, byte[])
     */
    @Override
    public void receive_attributes_read(int handle, int offset, int result,
            byte[] value) {
        out.printf(
                "ATT: receive_attributes_read(handle: 0x%04x, offset: %d, result: [0x%04x %s], value: %s)\n",
                handle, offset, result, reasonOrResult(result), hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_read_type(int,
     *      int, byte[])
     */
    @Override
    public void receive_attributes_read_type(int handle, int result,
            byte[] value) {
        out.printf(
                "ATT: receive_attributes_read_type(handle: 0x%04x, result: [0x%04x %s], value: %s)\n",
                handle, result, reasonOrResult(result), hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_user_response()
     */
    @Override
    public void receive_attributes_user_response() {
        out.printf("ATT: receive_attributes_user_response()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_value(int, int,
     *      int, int, byte[])
     */
    @Override
    public void receive_attributes_value(int connection, int reason, int handle,
            int offset, byte[] value) {
        out.printf(
                "ATT: receive_attributes_value(connection: %d, reason: [0x%04x %s], handle: 0x%04x, offset: %d, value: %s)\n",
                connection, reason, reasonOrResult(reason), handle, offset,
                hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attributes_user_request(int,
     *      int, int)
     */
    @Override
    public void receive_attributes_user_request(int connection, int handle,
            int offset) {
        out.printf(
                "ATT: receive_attributes_user_request(connection: %d, handle: 0x%04x, offset: %d)\n",
                connection, handle, offset);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_disconnect(int,
     *      int)
     */
    @Override
    public void receive_connection_disconnect(int connection, int result) {
        out.printf(
                "CONNECTION: receive_connection_disconnect(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_get_rssi(int,
     *      int)
     */
    @Override
    public void receive_connection_get_rssi(int connection, int rssi) {
        out.printf(
                "CONNECTION: receive_connection_get_rssi(connection: %d, rssi: %d dBm)\n",
                connection, rssi);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_update(int, int)
     */
    @Override
    public void receive_connection_update(int connection, int result) {
        out.printf(
                "CONNECTION: receive_connection_update(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_version_update(int,
     *      int)
     */
    @Override
    public void receive_connection_version_update(int connection, int result) {
        out.printf(
                "CONNECTION: receive_connection_version_update(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_channel_map_get(int,
     *      byte[])
     */
    @Override
    public void receive_connection_channel_map_get(int connection, byte[] map) {
        out.printf(
                "CONNECTION: receive_connection_channel_map_set(connection: %d, map: %s)\n",
                connection, hexDump(map));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_channel_map_set(int,
     *      int)
     */
    @Override
    public void receive_connection_channel_map_set(int connection, int result) {
        out.printf(
                "CONNECTION: receive_connection_channel_map_set(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_features_get(int,
     *      int)
     */
    @Override
    public void receive_connection_features_get(int connection, int result) {
        out.printf(
                "CONNECTION: receive_connection_features_get(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_get_status(int)
     */
    @Override
    public void receive_connection_get_status(int connection) {
        out.printf(
                "CONNECTION: receive_connection_get_status(connection: %d)\n",
                connection);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_raw_tx(int)
     */
    @Override
    public void receive_connection_raw_tx(int connection) {
        out.printf("CONNECTION: receive_connection_raw_tx(connection: %d)\n",
                connection);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_status(int, int,
     *      org.thingml.bglib.BDAddr, int, int, int, int, int)
     */
    @Override
    public void receive_connection_status(int connection, int flags,
            BDAddr address, int address_type, int conn_interval, int timeout,
            int latency, int bonding) {
        out.printf(
                "CONNECTION: receive_connection_status(connection: %d, flags: 0x%02x, address: %s, address_type: %s, conn_interval: %d, timeout: %d, latency: %d, bonding: 0x%02x)\n",
                connection, flags, address, addressType(address_type),
                conn_interval, timeout, latency, bonding);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_version_ind(int,
     *      int, int, int)
     */
    @Override
    public void receive_connection_version_ind(int connection, int vers_nr,
            int comp_id, int sub_vers_nr) {
        out.printf(
                "CONNECTION: receive_connection_version_ind(connection: %d, vers_nr: %d, comp_id: %d, sub_vers_nr: %d)\n",
                connection, vers_nr, comp_id, sub_vers_nr);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_feature_ind(int,
     *      byte[])
     */
    @Override
    public void receive_connection_feature_ind(int connection,
            byte[] features) {
        out.printf(
                "CONNECTION: receive_connection_feature_ind(connection: %d, features: %s)\n",
                hexDump(features));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_raw_rx(int,
     *      byte[])
     */
    @Override
    public void receive_connection_raw_rx(int connection, byte[] data) {
        out.printf(
                "CONNECTION: receive_connection_raw_rx(connection: %d, data: %s)\n",
                hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_connection_disconnected(int,
     *      int)
     */
    @Override
    public void receive_connection_disconnected(int connection, int reason) {
        out.printf(
                "CONNECTION: receive_connection_disconnected(connection: %d, reason: [0x%04x %s])\n",
                connection, reason, reasonOrResult(reason));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_find_by_type_value(int,
     *      int)
     */
    @Override
    public void receive_attclient_find_by_type_value(int connection,
            int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_find_by_type_value(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_by_group_type(int,
     *      int)
     */
    @Override
    public void receive_attclient_read_by_group_type(int connection,
            int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_by_group_type(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_by_type(int,
     *      int)
     */
    @Override
    public void receive_attclient_read_by_type(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_by_type(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_find_information(int,
     *      int)
     */
    @Override
    public void receive_attclient_find_information(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_find_information(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_by_handle(int,
     *      int)
     */
    @Override
    public void receive_attclient_read_by_handle(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_by_handle(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_attribute_write(int,
     *      int)
     */
    @Override
    public void receive_attclient_attribute_write(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_attribute_write(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_write_command(int,
     *      int)
     */
    @Override
    public void receive_attclient_write_command(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_write_command(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_reserved()
     */
    @Override
    public void receive_attclient_reserved() {
        out.printf("ATTCLIENT: receive_attclient_reserved()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_long(int,
     *      int)
     */
    @Override
    public void receive_attclient_read_long(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_long(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_prepare_write(int,
     *      int)
     */
    @Override
    public void receive_attclient_prepare_write(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_prepare_write(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_execute_write(int,
     *      int)
     */
    @Override
    public void receive_attclient_execute_write(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_execute_write(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_multiple(int,
     *      int)
     */
    @Override
    public void receive_attclient_read_multiple(int connection, int result) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_multiple(connection: %d, result: [0x%04x %s])\n",
                connection, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_indicated(int,
     *      int)
     */
    @Override
    public void receive_attclient_indicated(int connection, int attrhandle) {
        out.printf(
                "ATTCLIENT: receive_attclient_indicated(connection: %d, attrhandle: 0x%04x)\n",
                connection, attrhandle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_procedure_completed(int,
     *      int, int)
     */
    @Override
    public void receive_attclient_procedure_completed(int connection,
            int result, int chrhandle) {
        out.printf(
                "ATTCLIENT: receive_attclient_procedure_completed(connection: %d, result: [0x%04x %s], chrhandle: 0x%04x)\n",
                connection, result, reasonOrResult(result), chrhandle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_group_found(int,
     *      int, int, byte[])
     */
    @Override
    public void receive_attclient_group_found(int connection, int start,
            int end, byte[] uuid) {
        out.printf(
                "ATTCLIENT: receive_attclient_group_found(connection: %d, start: 0x%04x, end 0x%04x, uuid: %s)\n",
                connection, start, end, uuid(uuid));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_attribute_found(int,
     *      int, int, int, byte[])
     */
    @Override
    public void receive_attclient_attribute_found(int connection, int chrdecl,
            int value, int properties, byte[] uuid) {
        out.printf(
                "ATTCLIENT: receive_attclient_attribute_found(connection: %d, chrdecl: %d, value: %d, properties: %d, uuid: %s)\n",
                connection, chrdecl, value, properties, uuid(uuid));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_find_information_found(int,
     *      int, byte[])
     */
    @Override
    public void receive_attclient_find_information_found(int connection,
            int chrhandle, byte[] uuid) {
        out.printf(
                "ATTCLIENT: receive_attclient_find_information_found(connection: %d, chrhandle: 0x%04x, uuid: %s)\n",
                connection, chrhandle, uuid(uuid));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_attribute_value(int,
     *      int, int, byte[])
     */
    @Override
    public void receive_attclient_attribute_value(int connection, int atthandle,
            int type, byte[] value) {
        out.printf(
                "ATTCLIENT: receive_attclient_attribute_value(connection: %d, atthandle: 0x%04x, type: %d, value: %s)\n",
                connection, atthandle, type, hexDump(value));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_attclient_read_multiple_response(int,
     *      byte[])
     */
    @Override
    public void receive_attclient_read_multiple_response(int connection,
            byte[] handles) {
        out.printf(
                "ATTCLIENT: receive_attclient_read_multiple_response(connection: %d, handles: %s)\n",
                connection, hexDump(handles));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_encrypt_start(int, int)
     */
    @Override
    public void receive_sm_encrypt_start(int handle, int result) {
        out.printf(
                "SM: receive_sm_encrypt_start(handle: 0x%04x, result: [0x%04x %s])\n",
                handle, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_set_bondable_mode()
     */
    @Override
    public void receive_sm_set_bondable_mode() {
        out.printf("SM: receive_sm_set_bondable_mode()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_delete_bonding(int)
     */
    @Override
    public void receive_sm_delete_bonding(int result) {
        out.printf("SM: receive_sm_delete_bonding(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_set_parameters()
     */
    @Override
    public void receive_sm_set_parameters() {
        out.printf("SM: receive_sm_set_parameters()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_passkey_entry(int)
     */
    @Override
    public void receive_sm_passkey_entry(int result) {
        out.printf("SM: receive_sm_passkey_entry(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_get_bonds(int)
     */
    @Override
    public void receive_sm_get_bonds(int bonds) {
        out.printf("SM: receive_sm_get_bonds(bonds: %d)\n", bonds);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_set_oob_data()
     */
    @Override
    public void receive_sm_set_oob_data() {
        out.printf("SM: receive_sm_set_oob_data()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_smp_data(int, int,
     *      byte[])
     */
    @Override
    public void receive_sm_smp_data(int handle, int packet, byte[] data) {
        out.printf(
                "SM: receive_sm_smp_data(handle: 0x%04x, packet: %d, data: %s)\n",
                handle, packet, hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_bonding_fail(int, int)
     */
    @Override
    public void receive_sm_bonding_fail(int handle, int result) {
        out.printf(
                "SM: receive_sm_bonding_fail(handle: 0x%04x, result: [0x%04x %s])\n",
                handle, result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_passkey_display(int, int)
     */
    @Override
    public void receive_sm_passkey_display(int handle, int passkey) {
        out.printf(
                "SM: receive_sm_passkey_display(handle: 0x%04x, passkey: %d)\n",
                handle, passkey);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_passkey_request(int)
     */
    @Override
    public void receive_sm_passkey_request(int handle) {
        out.printf("SM: receive_sm_passkey_request(handle: 0x%04x)\n", handle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_sm_bond_status(int, int,
     *      int, int)
     */
    @Override
    public void receive_sm_bond_status(int bond, int keysize, int mitm,
            int keys) {
        out.printf(
                "SM: receive_sm_bond_status(bond: %d, keysize: %d, mitm: %d, keys: %d)\n",
                bond, keysize, mitm, keys);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_privacy_flags()
     */
    @Override
    public void receive_gap_set_privacy_flags() {
        out.printf("GAP: receive_gap_set_privacy_flags()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_mode(int)
     */
    @Override
    public void receive_gap_set_mode(int result) {
        out.printf("GAP: receive_gap_set_mode(result: [0x%04x %s])\n", result,
                reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_discover(int)
     */
    @Override
    public void receive_gap_discover(int result) {
        out.printf("GAP: receive_gap_discover(result: [0x%04x %s])\n", result,
                reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_connect_direct(int, int)
     */
    @Override
    public void receive_gap_connect_direct(int result, int connection_handle) {
        out.printf(
                "GAP: receive_gap_connect_direct(result: [0x%04x %s], connection_handle: %d)\n",
                result, reasonOrResult(result), connection_handle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_end_procedure(int)
     */
    @Override
    public void receive_gap_end_procedure(int result) {
        out.printf("GAP: receive_gap_end_procedure(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_connect_selective(int,
     *      int)
     */
    @Override
    public void receive_gap_connect_selective(int result,
            int connection_handle) {
        out.printf(
                "GAP: receive_gap_connect_selective(result: [0x%04x %s], connection_handle: %d)\n",
                result, reasonOrResult(result), connection_handle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_filtering(int)
     */
    @Override
    public void receive_gap_set_filtering(int result) {
        out.printf("GAP: receive_gap_set_filtering(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_scan_parameters(int)
     */
    @Override
    public void receive_gap_set_scan_parameters(int result) {
        out.printf(
                "GAP: receive_gap_set_scan_parameters(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_adv_parameters(int)
     */
    @Override
    public void receive_gap_set_adv_parameters(int result) {
        out.printf("GAP: receive_gap_set_adv_parameters(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_adv_data(int)
     */
    @Override
    public void receive_gap_set_adv_data(int result) {
        out.printf("GAP: receive_gap_set_adv_data(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_set_directed_connectable_mode(int)
     */
    @Override
    public void receive_gap_set_directed_connectable_mode(int result) {
        out.printf(
                "GAP: receive_gap_set_directed_connectable_mode(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_scan_response(int, int,
     *      org.thingml.bglib.BDAddr, int, int, byte[])
     */
    @Override
    public void receive_gap_scan_response(int rssi, int packet_type,
            BDAddr sender, int address_type, int bond, byte[] data) {
        out.printf(
                "GAP: receive_gap_scan_response(rssi: %d dBm, packet_type: [0x%02x %s], sender: %s, address_type: %s, bond: 0x%02x, data: %s)\n",
                rssi, packet_type, packetType(packet_type), sender,
                addressType(address_type), bond, hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_gap_mode_changed(int, int)
     */
    @Override
    public void receive_gap_mode_changed(int discover, int connect) {
        out.printf("GAP: receive_gap_mode_changed(discover: " + discover
                + ", connect: " + connect + ")\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_config_irq(int)
     */
    @Override
    public void receive_hardware_io_port_config_irq(int result) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_config_irq(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_set_soft_timer(int)
     */
    @Override
    public void receive_hardware_set_soft_timer(int result) {
        out.printf(
                "HARDWARE: receive_hardware_set_soft_timer(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_adc_read(int)
     */
    @Override
    public void receive_hardware_adc_read(int result) {
        out.printf("HARDWARE: receive_hardware_adc_read(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_config_direction(int)
     */
    @Override
    public void receive_hardware_io_port_config_direction(int result) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_config_direction(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_config_function(int)
     */
    @Override
    public void receive_hardware_io_port_config_function(int result) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_config_function(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_config_pull(int)
     */
    @Override
    public void receive_hardware_io_port_config_pull(int result) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_config_pull(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_write(int)
     */
    @Override
    public void receive_hardware_io_port_write(int result) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_write(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_read(int,
     *      int, int)
     */
    @Override
    public void receive_hardware_io_port_read(int result, int port, int data) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_read(result: [0x%04x %s], port: %d, data: %d)\n",
                result, reasonOrResult(result), port, data);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_spi_config(int)
     */
    @Override
    public void receive_hardware_spi_config(int result) {
        out.printf(
                "HARDWARE: receive_hardware_spi_config(result: [0x%04x %s])\n",
                result, reasonOrResult(result));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_spi_transfer(int,
     *      int, byte[])
     */
    @Override
    public void receive_hardware_spi_transfer(int result, int channel,
            byte[] data) {
        out.printf(
                "HARDWARE: receive_hardware_spi_transfer(result: [0x%04x %s], channel: %d, data: %s)\n",
                result, reasonOrResult(result), channel, hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_i2c_read(int,
     *      byte[])
     */
    @Override
    public void receive_hardware_i2c_read(int result, byte[] data) {
        out.printf(
                "HARDWARE: receive_hardware_i2c_read(result: [0x%04x %s], data: %s)\n",
                result, reasonOrResult(result), hexDump(data));
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_i2c_write(int)
     */
    @Override
    public void receive_hardware_i2c_write(int written) {
        out.printf("HARDWARE: receive_hardware_i2c_write(written: %d)\n",
                written);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_set_txpower()
     */
    @Override
    public void receive_hardware_set_txpower() {
        out.printf("HARDWARE: receive_hardware_set_txpower()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_io_port_status(int,
     *      int, int, int)
     */
    @Override
    public void receive_hardware_io_port_status(int timestamp, int port,
            int irq, int state) {
        out.printf(
                "HARDWARE: receive_hardware_io_port_status(timestamp: %d, port: %d, irq: %d, state: %d)\n",
                timestamp, port, irq, state);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_soft_timer(int)
     */
    @Override
    public void receive_hardware_soft_timer(int handle) {
        out.printf("HARDWARE: receive_hardware_soft_timer(handle: 0x%04x)\n",
                handle);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_hardware_adc_result(int,
     *      int)
     */
    @Override
    public void receive_hardware_adc_result(int input, int value) {
        out.printf(
                "HARDWARE: receive_hardware_adc_result(input: %d, value: %d)\n",
                input, value);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_test_phy_tx()
     */
    @Override
    public void receive_test_phy_tx() {
        out.printf("TEST: receive_test_phy_tx()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_test_phy_rx()
     */
    @Override
    public void receive_test_phy_rx() {
        out.printf("TEST: receive_test_phy_rx()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_test_phy_end(int)
     */
    @Override
    public void receive_test_phy_end(int counter) {
        out.printf("TEST: receive_test_phy_end(counter: %d)\n", counter);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_test_phy_reset()
     */
    @Override
    public void receive_test_phy_reset() {
        out.printf("TEST: receive_test_phy_reset()\n");
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_test_get_channel_map(byte[])
     */
    @Override
    public void receive_test_get_channel_map(byte[] channel_map) {
        out.printf("TEST: receive_test_get_channel_map(channel_map: %s)\n",
                hexDump(channel_map));
    }
}
