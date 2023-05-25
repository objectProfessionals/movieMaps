package com.op.moviemaps.script;

import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

/**
 * 
 * @author d07114915
 * 
 *         Class to get a mixer with a specified recordable audio format from a
 *         specified port For instance get a 44.1kHz 16bit record line for a
 *         "line in" input
 */
public class MixerMatcher {
	private static final String THE_INPUT_TYPE_I_WANT = Port.Info.MICROPHONE
			.getName();
	private static final String THE_NAME_OF_THE_MIXER_I_WANT_TO_GET_THE_INPUT_FROM = "Primary Sound Driver";
	private static final AudioFormat af = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 2 * 2, 44100.0F,
			false);
	private static final DataLine.Info targetDataLineInfo = new DataLine.Info(
			TargetDataLine.class, af);
	private static final Port.Info myInputType = new Port.Info((Port.class),
			THE_INPUT_TYPE_I_WANT, true);
	private static TargetDataLine targetDataLine = null;

	public static void main(String[] args) {
		// checkResources();
		Mixer portMixer = null;
		Mixer targetMixer = null;
		try {
			for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
				System.out.println("-" + mi.getName() + "-");
				if (mi.getName().equals(
						THE_NAME_OF_THE_MIXER_I_WANT_TO_GET_THE_INPUT_FROM)) {
					System.out.println("Trying to get portMixer for :"
							+ mi.getName());
					portMixer = getPortMixerInfoFor(mi);
					if (portMixer != null) {
						System.out.println(portMixer.getMixerInfo().toString());
						targetMixer = AudioSystem.getMixer(mi);
						break;
					}
				}
			}
			if (targetMixer != null) {
				targetMixer.open();

				targetDataLine = (TargetDataLine) targetMixer
						.getLine(targetDataLineInfo);
				System.out.println("Got TargetDataLine from :"
						+ targetMixer.getMixerInfo().getName());

				portMixer.open();

				Port port = (Port) portMixer.getLine(myInputType);
				port.open();

				Control[] controls = port.getControls();
				System.out.println((controls.length > 0 ? "Controls for the "
						+ THE_INPUT_TYPE_I_WANT + " port:"
						: "The port has no controls."));
				for (Control c : controls) {
					System.out.println(c.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// return the portMixer that corresponds to TargetMixer
	private static Mixer getPortMixerInfoFor(Mixer.Info mixerInfo) {
		// Check this out for interest
		// http://www.java-forum.org/spiele-multimedia-programmierung/94699-java-sound-api-zuordnung-port-mixer-input-mixer.html
		try {
			// get the requested mixer
			Mixer targetMixer = AudioSystem.getMixer(mixerInfo);
			targetMixer.open();
			// Check if it supports the desired format
			// if (targetMixer.isLineSupported(targetDataLineInfo)) {
			System.out.println(mixerInfo.getName() + " supports recording @ "
					+ af);
			// now go back and start again trying to match a mixer to a port
			// the only way I figured how is by matching name, because
			// the port mixer name is the same as the actual mixer with
			// "Port " in front of it
			// there MUST be a better way
			for (Mixer.Info portMixerInfo : AudioSystem.getMixerInfo()) {
				String port_string = "Port ";
				// if ((port_string + mixerInfo.getName()).equals(portMixerInfo
				// .getName())) {
				System.out.println("Matched Port to Mixer:"
						+ mixerInfo.getName() + "=" + portMixerInfo.getName());
				Mixer portMixer = AudioSystem.getMixer(portMixerInfo);
				portMixer.open();
				// now check the mixer has the right input type eg
				// LINE_IN
				boolean lineTypeSupported = portMixer
						.isLineSupported((Line.Info) myInputType);
				System.out.println(portMixerInfo.getName() + " does "
						+ (lineTypeSupported ? "" : "NOT") + " support "
						+ myInputType.getName());
				if (lineTypeSupported) {
					portMixer.close();
					targetMixer.close();
					return portMixer;
				}
				portMixer.close();
				// }
			}
			// }
			targetMixer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static boolean checkResources() {
		boolean retValue = true;
		try {
			System.out.println("microphone is supported "
					+ AudioSystem.isLineSupported(Port.Info.MICROPHONE));
			System.out.println("linein is supported "
					+ AudioSystem.isLineSupported(Port.Info.LINE_IN));
			System.out.println("speaker is supported "
					+ AudioSystem.isLineSupported(Port.Info.SPEAKER));
			System.out.println("lineout is supported "
					+ AudioSystem.isLineSupported(Port.Info.LINE_OUT));

			System.out.println("microphone is source "
					+ Port.Info.MICROPHONE.isSource());
			System.out.println("linein is source "
					+ Port.Info.LINE_IN.isSource());
			System.out.println("speaker is source "
					+ Port.Info.SPEAKER.isSource());
			System.out.println("lineout is source "
					+ Port.Info.LINE_OUT.isSource());
			Mixer.Info[] mixers = AudioSystem.getMixerInfo();
			System.out.println("amount of mixers is " + mixers.length);
			for (int m = 0; m < mixers.length; m++) {
				Mixer.Info mixerInfo = mixers[m];
				System.out.println("mixer's name [" + m + "] "
						+ mixerInfo.getName());
				System.out.println("mixer's description [" + m + "] "
						+ mixerInfo.getDescription());
				Mixer mixer = AudioSystem.getMixer(mixerInfo);

				Line.Info[] microphones = mixer
						.getTargetLineInfo(Port.Info.MICROPHONE);
				Line.Info[] lineins = mixer
						.getTargetLineInfo(Port.Info.LINE_IN);
				Line.Info[] speakers = mixer
						.getSourceLineInfo(Port.Info.SPEAKER);
				Line.Info[] lineouts = mixer
						.getSourceLineInfo(Port.Info.LINE_OUT);

				if (microphones != null) {
					System.out.println("amount of microphones is "
							+ microphones.length);
				}
				if (lineins != null) {
					System.out
							.println("amount of lineins is " + lineins.length);
				}
				if (speakers != null) {
					System.out.println("amount of speakers is "
							+ speakers.length);
				}
				if (lineouts != null) {
					System.out.println("amount of lineouts is "
							+ lineouts.length);
				}

				System.out.println("mixer [" + m + "] " + mixer);

				Line.Info[] lineInfos = mixer.getTargetLineInfo();
				System.out.println("    amount of target lines is "
						+ lineInfos.length);
				for (int l = 0; l < lineInfos.length; l++) {
					Line.Info lineInfo = lineInfos[l];
					System.out.println("target line's class name [" + l + "] "
							+ lineInfo.getClass());
					if (lineInfo instanceof DataLine.Info) {
						AudioFormat[] audioFormats = ((DataLine.Info) lineInfo)
								.getFormats();
						System.out
								.println("            amount of AudioFormat is "
										+ audioFormats.length);
						for (int a = 0; a < audioFormats.length; a++) {
							AudioFormat audioFormat = audioFormats[a];
							System.out.println("        audioFormat [" + a
									+ "] " + audioFormat);
						}
					}
				}
				lineInfos = mixer.getSourceLineInfo();
				System.out.println("    amount of source lines is "
						+ lineInfos.length);
				for (int l = 0; l < lineInfos.length; l++) {
					Line.Info lineInfo = lineInfos[l];
					System.out.println("     source line's class name [" + l
							+ "] " + lineInfo.getClass());
					if (lineInfo instanceof DataLine.Info) {
						AudioFormat[] audioFormats = ((DataLine.Info) lineInfo)
								.getFormats();
						System.out
								.println("            amount of AudioFormat is "
										+ audioFormats.length);
						for (int a = 0; a < audioFormats.length; a++) {
							AudioFormat audioFormat = audioFormats[a];
							System.out.println("        audioFormat [" + a
									+ "] " + audioFormat);
						}
					}
				}
			}
			System.out.println("Default mixer " + AudioSystem.getMixer(null));

			// System.exit(0);

			if (MidiSystem.getSequencer() == null) {
				System.err
						.println("MidiSystem Sequencer Unavailable, exiting!");
				retValue = false;
			} else if (AudioSystem.getMixer(null) == null) {
				// } else if (AudioSystem.getMixer(mixers[1]) == null) {
				System.err.println("AudioSystem Unavailable, exiting!");
				retValue = false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			retValue = false;
		}
		return retValue;
	}
}
