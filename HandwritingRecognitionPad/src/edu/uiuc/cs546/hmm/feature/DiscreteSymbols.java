package edu.uiuc.cs546.hmm.feature;

import be.ac.ulg.montefiore.run.jahmm.ObservationDiscrete;

// for new implementation, replacing QuantizedFeature.java
public enum DiscreteSymbols {

	// 256 discrete symbols in total

	Symbol0, Symbol1, Symbol2, Symbol3, Symbol4, Symbol5, Symbol6, Symbol7, Symbol8, Symbol9, Symbol10, Symbol11, Symbol12, Symbol13, Symbol14, Symbol15, Symbol16, Symbol17, Symbol18, Symbol19, Symbol20, Symbol21, Symbol22, Symbol23, Symbol24, Symbol25, Symbol26, Symbol27, Symbol28, Symbol29, Symbol30, Symbol31, Symbol32, Symbol33, Symbol34, Symbol35, Symbol36, Symbol37, Symbol38, Symbol39, Symbol40, Symbol41, Symbol42, Symbol43, Symbol44, Symbol45, Symbol46, Symbol47, Symbol48, Symbol49, Symbol50, Symbol51, Symbol52, Symbol53, Symbol54, Symbol55, Symbol56, Symbol57, Symbol58, Symbol59, Symbol60, Symbol61, Symbol62, Symbol63, Symbol64, Symbol65, Symbol66, Symbol67, Symbol68, Symbol69, Symbol70, Symbol71, Symbol72, Symbol73, Symbol74, Symbol75, Symbol76, Symbol77, Symbol78, Symbol79, Symbol80, Symbol81, Symbol82, Symbol83, Symbol84, Symbol85, Symbol86, Symbol87, Symbol88, Symbol89, Symbol90, Symbol91, Symbol92, Symbol93, Symbol94, Symbol95, Symbol96, Symbol97, Symbol98, Symbol99, Symbol100, Symbol101, Symbol102, Symbol103, Symbol104, Symbol105, Symbol106, Symbol107, Symbol108, Symbol109, Symbol110, Symbol111, Symbol112, Symbol113, Symbol114, Symbol115, Symbol116, Symbol117, Symbol118, Symbol119, Symbol120, Symbol121, Symbol122, Symbol123, Symbol124, Symbol125, Symbol126, Symbol127, Symbol128, Symbol129, Symbol130, Symbol131, Symbol132, Symbol133, Symbol134, Symbol135, Symbol136, Symbol137, Symbol138, Symbol139, Symbol140, Symbol141, Symbol142, Symbol143, Symbol144, Symbol145, Symbol146, Symbol147, Symbol148, Symbol149, Symbol150, Symbol151, Symbol152

	, Symbol153, Symbol154, Symbol155, Symbol156, Symbol157, Symbol158, Symbol159, Symbol160, Symbol161, Symbol162, Symbol163, Symbol164, Symbol165, Symbol166, Symbol167, Symbol168, Symbol169, Symbol170, Symbol171, Symbol172, Symbol173, Symbol174, Symbol175, Symbol176, Symbol177, Symbol178, Symbol179, Symbol180, Symbol181, Symbol182, Symbol183, Symbol184, Symbol185, Symbol186, Symbol187, Symbol188, Symbol189, Symbol190, Symbol191, Symbol192, Symbol193, Symbol194, Symbol195, Symbol196, Symbol197, Symbol198, Symbol199, Symbol200, Symbol201, Symbol202, Symbol203, Symbol204, Symbol205, Symbol206, Symbol207, Symbol208, Symbol209, Symbol210, Symbol211, Symbol212, Symbol213, Symbol214, Symbol215, Symbol216, Symbol217, Symbol218, Symbol219, Symbol220, Symbol221, Symbol222, Symbol223, Symbol224, Symbol225, Symbol226, Symbol227, Symbol228, Symbol229, Symbol230, Symbol231, Symbol232, Symbol233, Symbol234, Symbol235, Symbol236, Symbol237, Symbol238, Symbol239, Symbol240, Symbol241, Symbol242, Symbol243, Symbol244, Symbol245, Symbol246, Symbol247, Symbol248, Symbol249, Symbol250, Symbol251, Symbol252, Symbol253, Symbol254, Symbol255;

	public ObservationDiscrete<DiscreteSymbols> observation() {
		return new ObservationDiscrete<DiscreteSymbols>(this);
	}

	public static DiscreteSymbols get(int i) {
		return values()[i];
	}

}
